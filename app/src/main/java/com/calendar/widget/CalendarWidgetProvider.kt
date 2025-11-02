package com.calendar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews

class CalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "onUpdate() 호출됨")
        Log.d(TAG, "위젯 개수: ${appWidgetIds.size}")
        Log.d(TAG, "위젯 IDs: ${appWidgetIds.joinToString(", ")}")
        Log.d(TAG, "========================================")
        
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        Log.d(TAG, "onReceive() 호출됨 - Action: ${intent.action}")
        
        when (intent.action) {
            ACTION_REFRESH -> {
                Log.d(TAG, "새로고침 버튼 클릭됨")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, CalendarWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                
                // 모든 위젯에 로딩 표시
                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.calendar_widget)
                    views.setTextViewText(R.id.widget_title, "로딩 중...")
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
                    
                    // 데이터 새로고침
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.event_list)
                    
                    // 잠시 후 정상 업데이트 (날짜 표시 복원)
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }, 500)
                }
            }
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                Log.d(TAG, "날짜/시간 변경됨: ${intent.action}")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, CalendarWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                
                // 모든 위젯 업데이트
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.event_list)
                }
            }
        }
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled() - 첫 위젯 생성됨")
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "onDisabled() - 마지막 위젯 제거됨")
    }

    companion object {
        private const val TAG = "CalendarWidget"
        private const val ACTION_REFRESH = "com.calendar.widget.ACTION_REFRESH"
        
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            Log.d(TAG, "updateAppWidget() - ID: $appWidgetId")
            
            val views = RemoteViews(context.packageName, R.layout.calendar_widget)

            // 오늘 날짜를 타이틀로 설정
            val calendar = java.util.Calendar.getInstance()
            val dateFormat = java.text.SimpleDateFormat("yyyy년 M월 d일", java.util.Locale.KOREAN)
            val dayOfWeekFormat = java.text.SimpleDateFormat("E", java.util.Locale.KOREAN)
            val todayText = "${dateFormat.format(calendar.time)} (${dayOfWeekFormat.format(calendar.time)})"
            views.setTextViewText(R.id.widget_title, todayText)

            // RemoteViewsService를 통해 리스트 데이터 설정
            val intent = Intent(context, CalendarWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.event_list, intent)

            // 각 아이템 클릭 시 캘린더 앱으로 이동
            // Android 14+ 보안 정책: implicit Intent는 FLAG_IMMUTABLE 사용
            val clickIntent = Intent(Intent.ACTION_VIEW).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val clickPendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE or 
                    PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT
            )
            views.setPendingIntentTemplate(R.id.event_list, clickPendingIntent)

            // 타이틀 클릭 시에도 캘린더 앱 실행
            val calendarIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("content://com.android.calendar/time")
            }
            val calendarPendingIntent = PendingIntent.getActivity(
                context,
                0,
                calendarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, calendarPendingIntent)

            // 새로고침 버튼 클릭 시 위젯 업데이트
            val refreshIntent = Intent(context, CalendarWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.event_list)
            
            Log.d(TAG, "updateAppWidget() 완료 - ID: $appWidgetId")
        }
    }
}

