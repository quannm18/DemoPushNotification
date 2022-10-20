package com.example.demopushnotification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.demopushnotification.Notification.Companion.channelID
import com.example.demopushnotification.Notification.Companion.messageExtra
import com.example.demopushnotification.Notification.Companion.notificationID
import com.example.demopushnotification.Notification.Companion.titleExtra
import com.example.demopushnotification.databinding.ActivityMainBinding
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val a = R.layout.activity_main

        createNotificationChannel()
        binding.btnSet.setOnClickListener {
            scheduleNotification()
        }

        binding.btnOnceTime.setOnClickListener {
            myOneTimeWork()
        }

        binding.btnPeriodic.setOnClickListener {
            myPeriodicWork()
        }
    }

    private fun myOneTimeWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresCharging(false)
            .build()

        val myWorkRequest: WorkRequest = OneTimeWorkRequest
            .Builder(
                MyWorker::class.java,
            )
            .setInitialDelay(10L, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueue(myWorkRequest)
    }

    private fun myPeriodicWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        val myWorkRequest = PeriodicWorkRequest.Builder(
            MyWorker::class.java,
            10000000L,
            TimeUnit.MILLISECONDS
        ).setConstraints(constraints)
            .addTag("my_id")
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "my_id",
                ExistingPeriodicWorkPolicy.KEEP,
                myWorkRequest
            )
    }

    //--------------------------------------------------------


    //------------------------------------------------------------------

    private fun scheduleNotification() {
        val intent = Intent(applicationContext, Notification::class.java)
        val title = binding.edTitle.text.toString()
        val message = binding.edMessage.text.toString()
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)
        val time = getTime()
        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                notificationID,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager


            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        }

        showAlert(time, title, message)
    }

    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = DateFormat.getLongDateFormat(applicationContext)
        val timeFormat = DateFormat.getTimeFormat(applicationContext)

        AlertDialog.Builder(this)
            .setTitle("Notification")
            .setMessage(
                "Title:  $title\n" +
                        "Message: $message\n" +
                        "At: ${dateFormat.format(date)} ${timeFormat.format(date)}"
            )
            .setPositiveButton("OK") { _, _ -> }.show()
    }

    private fun getTime(): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val minute = binding.timePicker.minute
            val hour = binding.timePicker.hour

            val day = binding.datePicker.dayOfMonth
            val month = binding.datePicker.month
            val year = binding.datePicker.year

            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, hour, minute)
            return calendar.timeInMillis
        }
        return 0
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notify Channel"
            val desc = "A description of the Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance)
            channel.description = desc

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}