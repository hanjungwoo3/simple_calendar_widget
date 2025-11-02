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
            ""  // 종일 일정은 시간 표시 안 함
        } else {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.format(calendar.time)
        }

        views.setTextViewText(R.id.event_date, dateStr)
        views.setTextViewText(R.id.event_time, timeStr)
        views.setTextViewText(R.id.event_title, event.title)
        
        // 오늘 날짜인지 확인
        val today = Calendar.getInstance()
        val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                      calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                      calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

        // 날짜별 배경색 교대 적용을 위한 로직
        // 현재 날짜까지의 고유 날짜 개수를 세서 몇 번째 날짜인지 파악
        val uniqueDates = mutableListOf<String>()
        for (i in 0..position) {
            val checkCal = Calendar.getInstance().apply { timeInMillis = events[i].startTime }
            val checkDateKey = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.MONTH)}-${checkCal.get(Calendar.DAY_OF_MONTH)}"

            if (!uniqueDates.contains(checkDateKey)) {
                uniqueDates.add(checkDateKey)
            }
        }

        // 첫 번째 날짜(dateIndex=0)는 배경 있음, 두 번째 날짜(dateIndex=1)는 배경 없음, 계속 교차
        val dateIndex = uniqueDates.size - 1
        val useGrayBackground = dateIndex % 2 == 0
        
        // 배경색 설정: 오늘이면 녹색, 그 외는 날짜별로 회색/투명 교대
        when {
            isToday -> views.setInt(R.id.event_item_container, "setBackgroundColor", 0x66228B22)  // 어두운 녹색 (ForestGreen)
            useGrayBackground -> views.setInt(R.id.event_item_container, "setBackgroundColor", 0x33FFFFFF)  // 20% 흰색
            else -> views.setInt(R.id.event_item_container, "setBackgroundColor", 0x00000000)  // 투명
        }

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
        
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        Log.e(TAG, "========================================")
        Log.e(TAG, "[$timestamp] loadCalendarEvents() 시작")
        Log.e(TAG, "Android SDK: ${android.os.Build.VERSION.SDK_INT}")
        Log.e(TAG, "Package: ${context.packageName}")
        Log.e(TAG, "========================================")

        // 권한 체크
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        Log.e(TAG, "READ_CALENDAR 권한 상태: $hasPermission")
        
        if (!hasPermission) {
            Log.e(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            Log.e(TAG, "!!!!! 캘린더 권한이 없습니다 !!!!!")
            Log.e(TAG, "!!!!! 앱을 실행하여 권한을 허용해주세요 !!!!!")
            Log.e(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            
            // 권한 없음 메시지 표시용 더미 이벤트 추가
            events.add(CalendarEvent(
                id = -1,
                title = "⚠️ 캘린더 읽기 권한이 필요합니다. 앱을 실행하여 권한을 허용해주세요.",
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                allDay = true
            ))
            return
        }
        
        Log.e(TAG, "✅ 권한 확인 완료")

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

            Log.e(TAG, "📅 검색 기간 설정")
            Log.e(TAG, "  시작: ${Date(startTime)}")
            Log.e(TAG, "  종료: ${Date(endTime)}")

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

            Log.e(TAG, "🔍 ContentProvider 쿼리 시작")
            Log.e(TAG, "  URI: ${CalendarContract.Events.CONTENT_URI}")
            
            val cursor: Cursor? = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            val cursorExists = cursor != null
            val cursorCount = cursor?.count ?: 0
            Log.e(TAG, "📊 쿼리 결과")
            Log.e(TAG, "  Cursor 생성: $cursorExists")
            Log.e(TAG, "  일정 개수: $cursorCount")

            cursor?.use {
                val idColumn = it.getColumnIndex(CalendarContract.Events._ID)
                val titleColumn = it.getColumnIndex(CalendarContract.Events.TITLE)
                val startColumn = it.getColumnIndex(CalendarContract.Events.DTSTART)
                val endColumn = it.getColumnIndex(CalendarContract.Events.DTEND)
                val allDayColumn = it.getColumnIndex(CalendarContract.Events.ALL_DAY)

                var eventNum = 0
                while (it.moveToNext() && events.size < 20) { // 최대 20개까지
                    eventNum++
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "제목 없음"
                    val start = it.getLong(startColumn)
                    val end = it.getLong(endColumn)
                    val allDay = it.getInt(allDayColumn) == 1

                    val eventDateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                    Log.e(TAG, "  [$eventNum] 일정: $title")
                    Log.e(TAG, "      시작: ${eventDateFormat.format(Date(start))}")
                    Log.e(TAG, "      종일: $allDay")

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
            
            Log.e(TAG, "========================================")
            Log.e(TAG, "✅ 총 ${events.size}개 일정 로드 완료")
            Log.e(TAG, "========================================")
            
            if (events.isEmpty()) {
                Log.e(TAG, "⚠️ 주의: 일정이 없습니다!")
                Log.e(TAG, "  1. 디바이스에 캘린더 앱이 설치되어 있나요?")
                Log.e(TAG, "  2. 캘린더 앱에 일정이 등록되어 있나요?")
                Log.e(TAG, "  3. 동기화된 계정(구글 등)의 캘린더가 있나요?")
                
                // 일정 없음 메시지 표시
                events.add(CalendarEvent(
                    id = -2,
                    title = "📭 등록된 일정이 없습니다",
                    startTime = System.currentTimeMillis(),
                    endTime = System.currentTimeMillis(),
                    allDay = true
                ))
            }
        } catch (e: SecurityException) {
            // 권한이 없는 경우
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ SecurityException 발생!")
            Log.e(TAG, "메시지: ${e.message}")
            Log.e(TAG, "========================================")
            e.printStackTrace()
            
            events.add(CalendarEvent(
                id = -3,
                title = "⚠️ 보안 오류: 캘린더 접근 권한 문제",
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                allDay = true
            ))
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ Exception 발생!")
            Log.e(TAG, "타입: ${e.javaClass.simpleName}")
            Log.e(TAG, "메시지: ${e.message}")
            Log.e(TAG, "========================================")
            e.printStackTrace()
            
            events.add(CalendarEvent(
                id = -4,
                title = "❌ 오류 발생: ${e.message}",
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                allDay = true
            ))
        }
    }
}


