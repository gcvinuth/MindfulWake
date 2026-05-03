package com.mindfulwake.app.data

import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

object QuestionBank {

    private fun rand(min: Int, max: Int) = Random.nextInt(min, max + 1)
    private fun <T> pick(list: List<T>): T = list[Random.nextInt(list.size)]
    private fun <T> shuffle(list: List<T>): List<T> = list.shuffled()

    private fun nearbyWrong(correct: Int, count: Int = 3, minDist: Int = 1, maxDist: Int = 10): List<Int> {
        val wrongs = mutableSetOf<Int>()
        var attempts = 0
        while (wrongs.size < count && attempts < 100) {
            val offset = rand(minDist, maxDist) * if (Random.nextBoolean()) 1 else -1
            val w = correct + offset
            if (w != correct && w > 0) wrongs.add(w)
            attempts++
        }
        var fill = 1
        while (wrongs.size < count) { wrongs.add(correct + fill++); }
        return wrongs.take(count)
    }

    private fun nearbyWrongDouble(correct: Double, count: Int = 3, dist: Double = 5.0): List<Double> {
        val wrongs = mutableSetOf<Double>()
        var attempts = 0
        while (wrongs.size < count && attempts < 100) {
            val offset = (rand(1, dist.toInt()) * (if (Random.nextBoolean()) 1 else -1)).toDouble()
            val w = correct + offset
            if (w != correct && w > 0) wrongs.add(w)
            attempts++
        }
        var fill = 1.0
        while (wrongs.size < count) { wrongs.add(correct + fill); fill++ }
        return wrongs.take(count)
    }

    private fun fmt(question: String, correct: String, wrongs: List<String>, category: String, explanation: String): Question {
        val opts = shuffle(listOf(QuizOption(correct, true)) + wrongs.map { QuizOption(it, false) })
        return Question(question, opts, category, explanation)
    }

    // ── EASY ──
    private val easyGens: List<() -> Question> = listOf(
        {   // addition
            val a = rand(12, 49); val b = rand(12, 49); val ans = a + b
            fmt("What is $a + $b?", "$ans", nearbyWrong(ans, 3, 1, 8).map { "$it" }, "math", "$a + $b = $ans")
        },
        {   // multiplication
            val a = rand(3, 12); val b = rand(3, 12); val ans = a * b
            fmt("What is $a × $b?", "$ans", nearbyWrong(ans, 3, 1, 12).map { "$it" }, "math", "$a × $b = $ans")
        },
        {   // sequence
            val start = rand(2, 10); val step = rand(2, 6)
            val seq = (0..3).map { start + step * it }
            val ans = start + step * 4
            fmt("What comes next: ${seq.joinToString(", ")}, ?", "$ans", nearbyWrong(ans, 3, 1, step * 2).map { "$it" }, "pattern", "The pattern adds $step each time")
        },
        {   // days of week
            val days = listOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
            val i = rand(0, 6); val shift = rand(2, 4); val target = (i + shift) % 7
            val wrongs = days.filterIndexed { j, _ -> j != target }.shuffled().take(3)
            fmt("If today is ${days[i]}, what day is it $shift days from now?", days[target], wrongs, "logic", "$shift days after ${days[i]} is ${days[target]}")
        },
        {   // percentage
            val base = pick(listOf(50,100,200,250,400,500)); val pct = pick(listOf(10,20,25,50))
            val ans = base * pct / 100
            fmt("What is $pct% of $base?", "$ans", nearbyWrong(ans, 3, 1, max(5, (ans*0.3).roundToInt())).map { "$it" }, "math", "$pct% of $base = $ans")
        },
        {   // vowel count
            data class W(val word: String, val vowels: Int)
            val words = listOf(W("EDUCATION",5),W("BEAUTIFUL",5),W("STRENGTH",1),W("ALGORITHM",3),W("UMBRELLA",3),W("ORCHESTRA",3),W("KNOWLEDGE",3),W("BUTTERFLY",2),W("CHALLENGE",2),W("APPRECIATE",5))
            val w = pick(words)
            fmt("How many vowels (A,E,I,O,U) are in \"${w.word}\"?", "${w.vowels}", nearbyWrong(w.vowels, 3, 1, 2).map { "$it" }, "language", "The vowels in \"${w.word}\" count to ${w.vowels}")
        },
        {   // awareness
            data class AQ(val q: String, val correct: String, val wrongs: List<String>, val explain: String)
            val opts = listOf(
                AQ("Which of these is a proven benefit of waking up early?","Better mental clarity and focus", listOf("Increased need for caffeine","Lower body temperature all day","Reduced dream frequency"),"Early rising aligns with cortisol cycles."),
                AQ("What is the first thing experts recommend doing after waking up?","Hydrate with a glass of water", listOf("Check social media","Have a coffee immediately","Do intense exercise"),"Your body is dehydrated after sleep. Water kickstarts metabolism."),
                AQ("How many hours of sleep does the average adult need?","7-9 hours", listOf("4-5 hours","10-12 hours","5-6 hours"),"The National Sleep Foundation recommends 7-9 hours."),
                AQ("What hormone helps regulate your sleep-wake cycle?","Melatonin", listOf("Insulin","Adrenaline","Testosterone"),"Melatonin is produced by the pineal gland.")
            )
            val o = pick(opts); fmt(o.q, o.correct, o.wrongs, "awareness", o.explain)
        },
        {   // subtraction
            val a = rand(50, 150); val b = rand(10, a - 1); val ans = a - b
            fmt("What is $a − $b?", "$ans", nearbyWrong(ans, 3, 1, 10).map { "$it" }, "math", "$a − $b = $ans")
        }
    )

    // ── MEDIUM ──
    private val mediumGens: List<() -> Question> = listOf(
        {   // two-step arithmetic
            val a = rand(10, 30); val b = rand(5, 15); val c = rand(2, 8)
            val ans = a * b - c
            fmt("Solve: $a × $b − $c = ?", "$ans", nearbyWrong(ans, 3, 2, 15).map { "$it" }, "math", "$a×$b=${a*b}, then −$c=$ans")
        },
        {   // fraction to percentage
            data class F(val num: Int, val den: Int, val pct: Double)
            val fracs = listOf(F(3,4,75.0),F(2,5,40.0),F(1,8,12.5),F(5,8,62.5),F(3,5,60.0),F(7,10,70.0),F(1,3,33.3),F(2,3,66.7))
            val f = pick(fracs)
            val pctStr = if (f.pct % 1.0 == 0.0) "${f.pct.toInt()}%" else "${f.pct}%"
            val wrongs = nearbyWrongDouble(f.pct, 3, 5.0).map { if (it % 1.0 == 0.0) "${it.toInt()}%" else "$it%" }
            fmt("What is ${f.num}/${f.den} expressed as a percentage?", pctStr, wrongs, "math", "${f.num}/${f.den} = $pctStr")
        },
        {   // Fibonacci-like
            val a = rand(1, 5); val b = rand(1, 5)
            val seq = listOf(a, b, a+b, b+(a+b), (a+b)+(b+(a+b)))
            val ans = seq[3] + seq[4]
            fmt("Each number is sum of two before: ${seq.joinToString(", ")}, ?", "$ans", nearbyWrong(ans, 3, 1, 8).map { "$it" }, "pattern", "${seq[3]}+${seq[4]}=$ans")
        },
        {   // logic puzzles
            data class LQ(val q: String, val correct: String, val wrongs: List<String>, val explain: String)
            val qs = listOf(
                LQ("If all roses are flowers, and some flowers fade quickly, which MUST be true?","All roses are flowers", listOf("All flowers are roses","Some roses fade quickly","No roses fade quickly"),"Only the original premise is guaranteed true."),
                LQ("A clock shows 3:15. What is the angle between the hour and minute hands?","7.5 degrees", listOf("0 degrees","15 degrees","90 degrees"),"At 3:15, minute hand=90°, hour hand=97.5°, difference=7.5°."),
                LQ("If you overtake the person in 2nd place in a race, what position are you in?","2nd place", listOf("1st place","3rd place","It depends on the race"),"You take their position."),
                LQ("A bat and ball cost \$1.10. The bat costs \$1.00 more than the ball. How much is the ball?","\$0.05", listOf("\$0.10","\$0.15","\$0.01"),"Ball=\$0.05, Bat=\$1.05. Difference=\$1.00, total=\$1.10."),
                LQ("5 machines make 5 widgets in 5 minutes. How long for 100 machines to make 100 widgets?","5 minutes", listOf("100 minutes","1 minute","50 minutes"),"Each machine makes 1 widget in 5 min. 100 machines → 100 widgets in 5 min.")
            )
            val o = pick(qs); fmt(o.q, o.correct, o.wrongs, "logic", o.explain)
        },
        {   // letter pattern
            data class LP(val seq: List<String>, val answer: String, val explain: String)
            val patterns = listOf(
                LP(listOf("A","C","E","G","?"),"I","Skip one letter: A→C→E→G→I"),
                LP(listOf("Z","X","V","T","?"),"R","Go back by 2: Z→X→V→T→R"),
                LP(listOf("B","D","F","H","?"),"J","Every other letter: B→D→F→H→J"),
                LP(listOf("A","B","D","G","?"),"K","Gaps increase: +1,+2,+3,+4 → K")
            )
            val p = pick(patterns)
            val allLetters = ('A'..'Z').map { "$it" }
            val wrongs = allLetters.filter { it != p.answer && !p.seq.contains(it) }.shuffled().take(3)
            fmt("What letter comes next: ${p.seq.joinToString(", ")}", p.answer, wrongs, "pattern", p.explain)
        },
        {   // awareness medium
            data class AQ(val q: String, val correct: String, val wrongs: List<String>, val explain: String)
            val qs = listOf(
                AQ("What does the '20-20-20 rule' help prevent?","Eye strain from screens", listOf("Memory loss","Weight gain","Joint stiffness"),"Every 20 min, look 20 ft away for 20 seconds."),
                AQ("Which breathing technique is used for quick stress relief?","4-7-8 breathing", listOf("10-10-10 breathing","Rapid shallow breathing","Holding breath for 30s"),"Inhale 4s, hold 7s, exhale 8s."),
                AQ("What is 'sleep inertia'?","Grogginess felt right after waking up", listOf("Inability to fall asleep","Dreaming while awake","Sleeping too much"),"Transitional state between sleep and wakefulness."),
                AQ("Which part of the brain is responsible for decision-making and focus?","Prefrontal cortex", listOf("Cerebellum","Amygdala","Hippocampus"),"The prefrontal cortex handles executive functions."),
                AQ("What is the recommended duration for a power nap?","10-20 minutes", listOf("45-60 minutes","5 minutes or less","30-40 minutes"),"10-20 min keeps you in light sleep.")
            )
            val o = pick(qs); fmt(o.q, o.correct, o.wrongs, "awareness", o.explain)
        },
        {   // reverse calculation
            val product = pick(listOf(120,144,156,180,210,240,300,360))
            val a = rand(2, 12)
            if (product % a != 0) {
                val b = rand(2, 12); val ans = b * 12
                fmt("If $b × ? = $ans, what is the missing number?", "12", nearbyWrong(12,3,1,5).map{"$it"}, "math", "$ans ÷ $b = 12")
            } else {
                val ans = product / a
                fmt("If $a × ? = $product, what is the missing number?", "$ans", nearbyWrong(ans,3,1,10).map{"$it"}, "math", "$product ÷ $a = $ans")
            }
        },
        {   // analogies
            data class AN(val q: String, val correct: String, val wrongs: List<String>)
            val as_ = listOf(
                AN("Pen is to Writer as Brush is to _____","Painter", listOf("Singer","Builder","Dancer")),
                AN("Fish is to Water as Bird is to _____","Air", listOf("Tree","Nest","Ground")),
                AN("Eye is to See as Ear is to _____","Hear", listOf("Smell","Touch","Taste")),
                AN("Page is to Book as Key is to _____","Keyboard", listOf("Lock","Door","Safe")),
                AN("Seed is to Tree as Egg is to _____","Bird", listOf("Nest","Shell","Breakfast"))
            )
            val o = pick(as_); fmt(o.q, o.correct, o.wrongs, "language", "Analogy: ${o.correct} completes the relationship.")
        }
    )

    // ── HARD ──
    private val hardGens: List<() -> Question> = listOf(
        {   // multi-step
            val a = rand(12,25); val b = rand(3,9); val c = rand(10,30); val d = rand(2,5)
            val ans = a*b + c*d
            fmt("Solve: ($a × $b) + ($c × $d) = ?", "$ans", nearbyWrong(ans,3,5,25).map{"$it"}, "math", "($a×$b)+($c×$d)=${a*b}+${c*d}=$ans")
        },
        {   // square roots
            data class SQ(val n: Int, val root: Int)
            val bases = listOf(SQ(144,12),SQ(169,13),SQ(196,14),SQ(225,15),SQ(256,16),SQ(289,17),SQ(324,18),SQ(361,19),SQ(625,25))
            val b = pick(bases)
            fmt("What is the square root of ${b.n}?", "${b.root}", nearbyWrong(b.root,3,1,4).map{"$it"}, "math", "√${b.n} = ${b.root}")
        },
        {   // complex pattern
            data class CP(val seq: List<Int>, val ans: Int, val explain: String)
            val ps = listOf(
                CP(listOf(1,1,2,3,5,8,13),21,"Fibonacci: each number is sum of previous two"),
                CP(listOf(2,6,18,54),162,"Geometric: multiply by 3 each time"),
                CP(listOf(1,4,9,16,25),36,"Perfect squares: 1²,2²,3²,4²,5²,6²"),
                CP(listOf(1,8,27,64),125,"Perfect cubes: 1³,2³,3³,4³,5³"),
                CP(listOf(2,3,5,7,11,13),17,"Prime numbers sequence")
            )
            val p = pick(ps)
            fmt("What comes next: ${p.seq.joinToString(", ")}, ?", "${p.ans}", nearbyWrong(p.ans,3,1,max(3,(p.ans*0.15).roundToInt())).map{"$it"}, "pattern", p.explain)
        },
        {   // advanced logic
            data class LQ(val q: String, val correct: String, val wrongs: List<String>, val explain: String)
            val qs = listOf(
                LQ("Three switches control three bulbs. Visit once. How do you identify each switch?","Turn on switch 1, wait, turn off, turn on switch 2, then visit", listOf("Turn all on and visit","It's impossible with one visit","Turn switches on one at a time"),"Switch 1=warm off, Switch 2=on, Switch 3=cold off."),
                LQ("You have 8 balls, one heavier. Minimum balance weighings to find it?","2", listOf("3","4","1"),"Split 3-3-2. Compare groups of 3."),
                LQ("If A>B, B>C, C>D — which MUST be true?","A > D", listOf("D > A","B = C","A = D"),"By transitivity: A>B>C>D."),
                LQ("A lily pad doubles daily. Covers lake in 48 days. When does it cover half?","Day 47", listOf("Day 24","Day 46","Day 36"),"Half-covered the day before fully covered.")
            )
            val o = pick(qs); fmt(o.q, o.correct, o.wrongs, "logic", o.explain)
        },
        {   // verbal
            data class VQ(val q: String, val correct: String, val wrongs: List<String>, val explain: String)
            val qs = listOf(
                VQ("Which word does NOT belong: Apple, Banana, Tomato, Cherry?","Tomato", listOf("Apple","Banana","Cherry"),"Tomato is botanically fruit but culinarily a vegetable."),
                VQ("What word becomes shorter when you add two letters to it?","Short", listOf("Long","Small","Tiny"),"'Short' + 'er' = 'Shorter'."),
                VQ("Rearrange 'CIFAIPC' → A large body of water","PACIFIC", listOf("ATLANTIC","GLACIAL","CAPITAL"),"CIFAIPC = PACIFIC."),
                VQ("I have cities but no houses, mountains but no trees, water but no fish. What am I?","A map", listOf("A painting","A dream","A book"),"A map has representations but not the real things.")
            )
            val o = pick(qs); fmt(o.q, o.correct, o.wrongs, "language", o.explain)
        },
        {   // time calculation
            val h = rand(1,11); val m = rand(0,5)*10; val addH = rand(2,8); val addM = rand(1,5)*10
            val totalM = h*60 + m + addH*60 + addM
            val ansH = (totalM/60) % 12
            val finalH = if (ansH == 0) 12 else ansH
            val ansMin = totalM % 60
            val ampm = if (totalM >= 720) "PM" else "AM"
            val ans = "%d:%02d %s".format(finalH, ansMin, ampm)
            val w1 = "%d:%02d %s".format((finalH % 12) + 1, ansMin, ampm)
            val w2 = "%d:%02d %s".format(finalH, (ansMin + 10) % 60, ampm)
            val w3 = "%d:%02d %s".format((finalH + 1) % 12 + 1, (ansMin + 20) % 60, if (ampm == "AM") "PM" else "AM")
            fmt("If it's %d:%02d AM and you add $addH hours and $addM minutes, what time is it?".format(h, m), ans, listOf(w1,w2,w3), "math", "%d:%02d + ${addH}h ${addM}m = $ans".format(h,m))
        },
        {   // deep awareness
            data class AQ(val q: String, val correct: String, val wrongs: List<String>, val explain: String)
            val qs = listOf(
                AQ("What is 'neuroplasticity'?","The brain's ability to reorganize and form new connections", listOf("A type of brain surgery","A mental illness","The hardening of brain tissue"),"Neuroplasticity allows the brain to adapt and learn."),
                AQ("The 'flow state' is best described as:","Being fully immersed and focused on an activity", listOf("Feeling sleepy and relaxed","Multi-tasking efficiently","Daydreaming productively"),"Flow state = optimal focus where time seems to stop."),
                AQ("What is the 'Zeigarnik Effect'?","Uncompleted tasks are remembered better than completed ones", listOf("Completed tasks feel more satisfying","People forget tasks when interrupted","Work expands to fill available time"),"Explains why unfinished tasks nag our memory."),
                AQ("Which practice increases gray matter in the brain?","Meditation", listOf("Watching educational videos","Drinking coffee","Sleeping more than 12 hours"),"Regular meditation increases gray matter density.")
            )
            val o = pick(qs); fmt(o.q, o.correct, o.wrongs, "awareness", o.explain)
        },
        {   // price increase
            val orig = pick(listOf(80,120,150,200,250))
            val pct = pick(listOf(15,20,25,30,40))
            val ans = orig + orig * pct / 100
            fmt("A price of \$$orig increases by $pct%. What is the new price?", "\$$ans", nearbyWrong(ans,3,5,20).map{"\$$it"}, "math", "\$$orig + $pct% = \$$ans")
        }
    )

    private val generators = mapOf(
        "easy" to easyGens,
        "medium" to mediumGens,
        "hard" to hardGens
    )

    fun generate(difficulty: String, count: Int): List<Question> {
        val gens = generators[difficulty] ?: generators["medium"]!!
        val questions = mutableListOf<Question>()
        val used = mutableSetOf<Int>()
        var attempts = 0
        while (questions.size < count && attempts < count * 10) {
            val idx = Random.nextInt(gens.size)
            if (used.size < gens.size && used.contains(idx)) { attempts++; continue }
            used.add(idx)
            try { val q = gens[idx](); questions.add(q) } catch (e: Exception) { /* skip */ }
            attempts++
        }
        while (questions.size < count) {
            try { questions.add(pick(gens)()) } catch (e: Exception) { break }
        }
        return questions
    }

    val categoryInfo = mapOf(
        "math" to Pair("🔢", "Math"),
        "logic" to Pair("🧠", "Logic"),
        "awareness" to Pair("🧘", "Awareness"),
        "pattern" to Pair("🔗", "Pattern"),
        "language" to Pair("📝", "Language"),
        "custom" to Pair("⭐", "Custom")
    )

    val morningQuotes = listOf(
        Pair("The secret of getting ahead is getting started.", "Mark Twain"),
        Pair("The way to get started is to quit talking and begin doing.", "Walt Disney"),
        Pair("Today is a new day. Don't let your history interfere with your destiny.", "Steve Maraboli"),
        Pair("Write it on your heart that every day is the best day in the year.", "Ralph Waldo Emerson"),
        Pair("Wake up with determination. Go to bed with satisfaction.", "Unknown"),
        Pair("Great things never come from comfort zones.", "Unknown"),
        Pair("Dream it. Wish it. Do it.", "Unknown"),
        Pair("Push yourself, because no one else is going to do it for you.", "Unknown")
    )

    fun getQuote() = pick(morningQuotes)
}
