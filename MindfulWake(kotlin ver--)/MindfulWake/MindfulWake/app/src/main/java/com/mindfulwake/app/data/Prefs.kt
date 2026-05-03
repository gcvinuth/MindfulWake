package com.mindfulwake.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Prefs {
    private const val FILE = "mindfulwake_prefs"
    private const val KEY_ALARMS = "alarms"
    private const val KEY_SETTINGS = "settings"
    private const val KEY_STATS = "stats"
    private val gson = Gson()

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // ── Alarms ──
    fun saveAlarms(ctx: Context, alarms: List<Alarm>) {
        sp(ctx).edit().putString(KEY_ALARMS, gson.toJson(alarms)).apply()
    }

    fun loadAlarms(ctx: Context): MutableList<Alarm> {
        val json = sp(ctx).getString(KEY_ALARMS, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<Alarm>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    // ── Settings ──
    fun saveSettings(ctx: Context, s: AppSettings) {
        sp(ctx).edit().putString(KEY_SETTINGS, gson.toJson(s)).apply()
    }

    fun loadSettings(ctx: Context): AppSettings {
        val json = sp(ctx).getString(KEY_SETTINGS, null) ?: return AppSettings()
        return try { gson.fromJson(json, AppSettings::class.java) } catch (e: Exception) { AppSettings() }
    }

    // ── Stats ──
    fun saveStats(ctx: Context, s: Stats) {
        sp(ctx).edit().putString(KEY_STATS, gson.toJson(s)).apply()
    }

    fun loadStats(ctx: Context): Stats {
        val json = sp(ctx).getString(KEY_STATS, null) ?: return Stats()
        return try { gson.fromJson(json, Stats::class.java) } catch (e: Exception) { Stats() }
    }
}
