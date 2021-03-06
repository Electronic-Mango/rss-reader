package com.lazureleming.rssreader

import android.app.Notification
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_DEFAULT
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import androidx.work.BackoffPolicy.EXPONENTIAL
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.PeriodicWorkRequest.MIN_BACKOFF_MILLIS
import com.lazureleming.rssreader.R.drawable.ic_notification
import com.lazureleming.rssreader.rssentries.RssEntry
import com.lazureleming.rssreader.rssentries.toLocalDateTime
import com.lazureleming.rssreader.utils.Common.INTENT_FROM_NOTIFICATION
import com.lazureleming.rssreader.utils.Common.LATEST_LOADED_RSS_ENTRY
import com.lazureleming.rssreader.utils.Common.NOTIFICATION_CHANNEL_ID
import com.lazureleming.rssreader.utils.Common.RSS_SOURCE
import com.lazureleming.rssreader.utils.Common.SHARED_PREFERENCES_LOCATION
import com.lazureleming.rssreader.utils.RemoteResourcesLoader.loadBitmap
import com.lazureleming.rssreader.utils.RemoteResourcesLoader.tryToLoadAndParseRssData
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit.MILLISECONDS

private const val WORK_NAME = "WORK_CHECK_UPDATES"
private const val INTERVAL_MS = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
private const val FLEX_MS = PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS

/**
 * Helper method adding new [CheckUpdatesWorker] for [PeriodicWorkRequest] and adding it to queue via [WorkManager].
 */
fun setupWorker(context: Context, latestRssEntry: RssEntry, rssLink: String) {
    context.getSharedPreferences(SHARED_PREFERENCES_LOCATION, MODE_PRIVATE).edit()
        .putString(LATEST_LOADED_RSS_ENTRY, latestRssEntry.date?.toString())
        .putString(RSS_SOURCE, rssLink)
        .apply()
    PeriodicWorkRequestBuilder<CheckUpdatesWorker>(INTERVAL_MS, MILLISECONDS, FLEX_MS, MILLISECONDS)
        .setBackoffCriteria(EXPONENTIAL, MIN_BACKOFF_MILLIS, MILLISECONDS)
        .build().let { periodicWorkRequest ->
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, REPLACE, periodicWorkRequest)
        }
}

/**
 * Helper method deleting [CheckUpdatesWorker] from [WorkManager].
 */
fun cancelWorker(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
}

/**
 * [Worker] looking for new RSS entries and showing notifications for updates.
 */
class CheckUpdatesWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val sharedPreferences by lazy { context.getSharedPreferences(SHARED_PREFERENCES_LOCATION, MODE_PRIVATE) }

    override fun doWork(): Result {
        val lastReadRssEntryDate = getLastRssEntryDate() ?: return Result.failure()
        val rssSource = sharedPreferences.getString(RSS_SOURCE, null) ?: return Result.failure()
        return if (tryToLoadRssDataAndNotify(rssSource, lastReadRssEntryDate)) Result.success() else Result.retry()
    }

    private fun getLastRssEntryDate(): LocalDateTime? {
        return sharedPreferences.getString(LATEST_LOADED_RSS_ENTRY, null)?.toLocalDateTime()
    }

    private fun tryToLoadRssDataAndNotify(rssSource: String, lastReadRssEntryDate: LocalDateTime): Boolean {
        return tryToLoadAndParseRssData(rssSource, { handleLoadedRssData(it, lastReadRssEntryDate) }, { })
    }

    private fun handleLoadedRssData(loadedRssEntries: List<RssEntry>, lastReadRssEntryDate: LocalDateTime) {
        loadedRssEntries.asSequence()
            .filter { rssEntry -> rssEntry.date?.let { it > lastReadRssEntryDate } ?: false }
            .onEach { it.image = loadBitmap(it.getSmallestImageUrl()) }
            .onEach { NotificationManagerCompat.from(context).notify(it.hashCode(), it.createNotification()) }
            .mapNotNull(RssEntry::date)
            .map(LocalDateTime::toString)
            .firstOrNull()?.let { sharedPreferences.edit().putString(LATEST_LOADED_RSS_ENTRY, it).apply() }
    }

    private fun RssEntry.createNotification(): Notification {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(ic_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setLargeIcon(image)
            .setPriority(PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createOpenDetailsIntent())
            .build()
    }

    private fun RssEntry.createOpenDetailsIntent(): PendingIntent? {
        val detailsIntent = toIntent(context, RssEntryDetailsActivity::class).apply {
            putExtra(INTENT_FROM_NOTIFICATION, true)
        }
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(detailsIntent)
            .getPendingIntent(hashCode(), PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
