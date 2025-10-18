package com.calendar.widget

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class CalendarWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return CalendarRemoteViewsFactory(this.applicationContext)
    }
}

class CalendarRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private val events = mutableListOf<CalendarEvent>()
    private val TAG = "CalendarWidget"
    
    data class CalendarEvent(
        val id: Long,
        val title: String,
        val startTime: Long,
        val endTime: Long,
        val allDay: Boolean
    )

    override fun onCreate() {
        // 초기화
    }

    override fun onDataSetChanged() {
        // 캘린더 데이터 로드 (내부에서 clear 수행)
        loadCalendarEvents()
    }

    override fun onDestroy() {
        events.clear()
    }

    override fun getCount(): Int = events.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.event_item)
        
        if (position >= events.size) {
            return views
        }

        val event = events[position]
        val calendar = Calendar.getInstance().apply {
            timeInMillis = event.startTime
        }

        // 날짜 포맷 (MM/dd(요일))
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val dayOfWeekFormat = SimpleDateFormat("E", Locale.KOREAN)
        val dateStr = "${dateFormat.format(calendar.time)}(${dayOfWeekFormat.format(calendar.time)})"

        // 시간 포맷 (HH:mm)
        val timeStr = if (event.allDay) {
            "종일"
        } else {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.format(calendar.time)
        }

        views.setTextViewText(R.id.event_date, dateStr)
        views.setTextViewText(R.id.event_time, timeStr)
        views.setTextViewText(R.id.event_title, event.title)

        // 각 아이템 클릭 시 구글 캘린더 앱으로 이동
        val fillInIntent = Intent().apply {
            // 이벤트 상세 화면으로 이동하는 URI
            data = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                event.id
            )
            // 시작 시간과 종료 시간 추가
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endTime)
        }
        
        // 전체 아이템 컨테이너에 클릭 이벤트 설정
        views.setOnClickFillInIntent(R.id.event_item_container, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    private fun loadCalendarEvents() {
        events.clear()
        
        Log.e(TAG, "========== loadCalendarEvents() 시작 ==========")
        System.out.println("CalendarWidget: loadCalendarEvents() 시작")

        // 권한 체크
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "!!!!! 캘린더 권한이 없습니다 !!!!!")
            System.out.println("CalendarWidget: 권한 없음")
            return
        }
        
        Log.e(TAG, "권한 확인 완료")

        try {
            // 오늘 시작 시간
            val startTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // 3개월 후까지
            val endTime = Calendar.getInstance().apply {
                add(Calendar.MONTH, 3)
            }.timeInMillis

            Log.e(TAG, "검색 기간: ${Date(startTime)} ~ ${Date(endTime)}")
            System.out.println("CalendarWidget: 검색 기간 설정 완료")

            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.ALL_DAY
            )

            val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
            val selectionArgs = arrayOf(startTime.toString(), endTime.toString())
            val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

            val cursor: Cursor? = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            Log.e(TAG, "Cursor 생성: ${cursor != null}, count: ${cursor?.count}")
            System.out.println("CalendarWidget: Cursor count = ${cursor?.count}")

            cursor?.use {
                val idColumn = it.getColumnIndex(CalendarContract.Events._ID)
                val titleColumn = it.getColumnIndex(CalendarContract.Events.TITLE)
                val startColumn = it.getColumnIndex(CalendarContract.Events.DTSTART)
                val endColumn = it.getColumnIndex(CalendarContract.Events.DTEND)
                val allDayColumn = it.getColumnIndex(CalendarContract.Events.ALL_DAY)

                while (it.moveToNext() && events.size < 20) { // 최대 20개까지
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "제목 없음"
                    val start = it.getLong(startColumn)
                    val end = it.getLong(endColumn)
                    val allDay = it.getInt(allDayColumn) == 1

                    Log.e(TAG, "일정 발견: $title, ${Date(start)}")
                    System.out.println("CalendarWidget: 일정 - $title")

                    events.add(
                        CalendarEvent(
                            id = id,
                            title = title,
                            startTime = start,
                            endTime = end,
                            allDay = allDay
                        )
                    )
                }
            }
            
            Log.e(TAG, "========== 총 ${events.size}개 일정 로드 완료 ==========")
            System.out.println("CalendarWidget: 총 ${events.size}개 일정")
        } catch (e: SecurityException) {
            // 권한이 없는 경우
            Log.e(TAG, "SecurityException: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            e.printStackTrace()
        }
    }
}


