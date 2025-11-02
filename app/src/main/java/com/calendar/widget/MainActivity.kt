package com.calendar.widget

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private val CALENDAR_PERMISSION_REQUEST = 100
    private val GITHUB_RELEASES_URL = "https://github.com/hanjungwoo3/simple_calendar_widget/releases"
    private val TAG = "CalendarWidget"
    
    private lateinit var currentVersionText: TextView
    private lateinit var githubButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "MainActivity onCreate()")
        Log.d(TAG, "Android SDK: ${Build.VERSION.SDK_INT}")
        Log.d(TAG, "Package: $packageName")
        Log.d(TAG, "========================================")
        
        // 뷰 초기화
        currentVersionText = findViewById(R.id.current_version_text)
        githubButton = findViewById(R.id.github_button)
        
        // 현재 버전 표시
        val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName
        currentVersionText.text = "현재 버전: $currentVersion\n\nAndroid ${Build.VERSION.SDK_INT} (API ${Build.VERSION.SDK_INT})"
        
        // GitHub 버튼 리스너 설정
        githubButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_RELEASES_URL))
            startActivity(intent)
        }
        
        // 캘린더 권한 요청
        checkCalendarPermission()
    }
    
    private fun checkCalendarPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        
        Log.d(TAG, "캘린더 권한 상태: $hasPermission")
        
        if (!hasPermission) {
            Log.d(TAG, "권한 요청 시작")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALENDAR),
                CALENDAR_PERMISSION_REQUEST
            )
        } else {
            Log.d(TAG, "권한이 이미 허용됨 - 위젯 업데이트 시작")
            updateAllWidgets()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            CALENDAR_PERMISSION_REQUEST -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                
                Log.d(TAG, "========================================")
                Log.d(TAG, "권한 요청 결과: ${if (granted) "허용" else "거부"}")
                Log.d(TAG, "========================================")
                
                if (granted) {
                    Toast.makeText(this, "✅ 캘린더 권한이 허용되었습니다.\n위젯이 업데이트됩니다.", Toast.LENGTH_LONG).show()
                    updateAllWidgets()
                } else {
                    Toast.makeText(this, "⚠️ 캘린더 권한이 필요합니다.\n설정에서 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "권한 거부됨 - 위젯이 제대로 작동하지 않습니다")
                }
            }
        }
    }
    
    private fun updateAllWidgets() {
        Log.d(TAG, "모든 위젯 업데이트 시작")
        
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, CalendarWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        Log.d(TAG, "설치된 위젯 개수: ${appWidgetIds.size}")
        
        if (appWidgetIds.isNotEmpty()) {
            // 위젯 업데이트 트리거
            val intent = Intent(this, CalendarWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            sendBroadcast(intent)
            Log.d(TAG, "위젯 업데이트 브로드캐스트 전송 완료")
        } else {
            Log.d(TAG, "설치된 위젯이 없습니다")
        }
    }
}


