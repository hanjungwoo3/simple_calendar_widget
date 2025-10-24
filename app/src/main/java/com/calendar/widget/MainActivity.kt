package com.calendar.widget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private val CALENDAR_PERMISSION_REQUEST = 100
    private val GITHUB_RELEASES_URL = "https://github.com/hanjungwoo3/simple_calendar_widget/releases"
    
    private lateinit var currentVersionText: TextView
    private lateinit var githubButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // 뷰 초기화
        currentVersionText = findViewById(R.id.current_version_text)
        githubButton = findViewById(R.id.github_button)
        
        // 현재 버전 표시
        val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName
        currentVersionText.text = "현재 버전: $currentVersion"
        
        // GitHub 버튼 리스너 설정
        githubButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_RELEASES_URL))
            startActivity(intent)
        }
        
        // 캘린더 권한 요청
        checkCalendarPermission()
    }
    
    private fun checkCalendarPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CALENDAR),
                CALENDAR_PERMISSION_REQUEST
            )
        }
    }
}


