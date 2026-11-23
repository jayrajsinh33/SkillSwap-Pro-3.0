# SkillSwap local API smoke test (DEMO_MODE=true only).
# Start backend on 8080 first, then run from the project root:
# powershell -ExecutionPolicy Bypass -File .\test-local-windows.ps1
$ErrorActionPreference = 'Stop'
$Base = 'http://localhost:8081/api'
$Passed = 0
function Check([bool]$Condition, [string]$Label) {
    if (-not $Condition) { throw "FAILED: $Label" }
    $script:Passed++
    Write-Host "PASS: $Label" -ForegroundColor Green
}
function Get-Api($Route, $Headers = @{}) {
    return Invoke-RestMethod -Method Get -Uri "$Base$Route" -Headers $Headers -TimeoutSec 20
}
function Post-Api($Route, $Data, $Headers = @{}) {
    return Invoke-RestMethod -Method Post -Uri "$Base$Route" -Headers $Headers -ContentType 'application/json' -Body ($Data | ConvertTo-Json -Depth 8 -Compress) -TimeoutSec 20
}
try {
    $health = Get-Api '/health'
    Check ($health.status -eq 'ok') 'Spring API health'

    $courses = @(Get-Api '/catalog/courses')
    Check ($courses.Count -ge 20) '20 or more demo courses available'

    # All test accounts are randomly generated. No real payment or payout happens.
    $email = "qa-$([guid]::NewGuid().ToString('N').Substring(0,12))@example.test"
    $account = Post-Api '/auth/register' @{name='SkillSwap QA Student';email=$email;password='DemoTest12345!'}
    Check (-not [string]::IsNullOrWhiteSpace($account.token)) 'Register learner and receive token'
    $h = @{Authorization="Bearer $($account.token)"}

    $me = Get-Api '/auth/me' $h
    Check ($me.email -eq $email) 'Authenticated account profile'

    $walletBefore = Get-Api '/wallet' $h
    Check ($walletBefore.balancePaise -eq 0) 'New account starts with empty wallet'
    $credit = Post-Api '/wallet/demo-topup' @{amountPaise=200000} $h
    Check ($credit.balancePaise -eq 200000) 'Local demo wallet top-up'

    $groups = @(Get-Api '/group-classes' $h)
    Check ($groups.Count -ge 1) 'Group workshops exist'

    $courseId = [int]$groups[0].courseId
    $quote = Get-Api "/checkout/$courseId" $h
    Check ($quote.quote.totalPaise -gt 0 -and $quote.quote.totalPaise -le 200000) 'Course checkout quote'
    $purchase = Post-Api "/checkout/$courseId" @{} $h
    Check ($purchase.enrollmentId -gt 0) 'Paid enrollment via demo wallet'

    $lessonPreview = @(Get-Api "/catalog/courses/$courseId/syllabus")
    Check ($lessonPreview.Count -ge 6) 'Course syllabus has actual lesson records'
    $learning = Get-Api "/learning/course/$courseId" $h
    Check ($learning.totalLessons -ge 6) 'Enrolled student can open complete lesson workspace'
    $firstLessonId = [int]$learning.lessons[0].id
    $completed = Post-Api "/learning/lessons/$firstLessonId/completion" @{completed=$true} $h
    Check ($completed.progress -gt 0) 'Lesson completion saved and enrollment progress updated'

    $walletAfter = Get-Api '/wallet' $h
    Check ($walletAfter.balancePaise -eq (200000 - $quote.quote.totalPaise)) 'Wallet debit matches checkout quote'
    Check ($walletAfter.withdrawablePaise -eq 0) 'Learner demo credits are not eligible teaching payouts'

    $registered = Post-Api "/group-classes/$($groups[0].id)/register" @{} $h
    Check ($registered.registered -eq $true) 'Student registered for purchased course workshop'
    $room = Get-Api "/group-classes/$($groups[0].id)/join" $h
    Check (-not [string]::IsNullOrWhiteSpace($room.roomName)) 'Demo group class join authorization'

    $course = @($courses | Where-Object {$_.id -eq $courseId})[0]
    $otherId = $course.mentor.id
    $chat = Post-Api "/chats/direct/$otherId" @{} $h
    Check ($chat.id -gt 0) 'Create personal student-to-mentor chat'
    $sent = Post-Api "/chats/$($chat.id)/messages" @{body='Automated local smoke test message'} $h
    Check ($sent.sent -eq $true) 'Persist a new message'
    $history = @(Get-Api "/chats/$($chat.id)/messages" $h)
    Check ($history.Count -ge 1) 'Read persisted chat messages'

    $people = @(Get-Api '/users/directory' $h)
    Check ($people.Count -ge 6) 'Authenticated people directory endpoint'

    $admin = Post-Api '/auth/login' @{email='admin@skillswap.local';password='Demo12345!'}
    $adminHeaders = @{Authorization="Bearer $($admin.token)"}
    $overview = Get-Api '/admin/overview' $adminHeaders
    Check ($overview.users -ge 8) 'Admin overview authorized'

    # Confirm a 1-to-1 booking with admin approval, then obtain its room.
    $futureStart = [DateTime]::UtcNow.AddHours(3).ToString('yyyy-MM-ddTHH:mm:ssZ')
    $booking = Post-Api '/bookings' @{courseId=$courseId;startAt=$futureStart} $h
    Check ($booking.status -eq 'PENDING') 'Student requests a one-to-one session'
    $confirmation = Invoke-RestMethod -Method Patch -Uri "$Base/bookings/$($booking.id)" -Headers $adminHeaders -ContentType 'application/json' -Body '{"status":"CONFIRMED"}' -TimeoutSec 20
    Check ($confirmation.status -eq 'CONFIRMED') 'Mentor/admin confirmation workflow'
    $personalRoom = Get-Api "/bookings/$($booking.id)/join" $h
    Check (-not [string]::IsNullOrWhiteSpace($personalRoom.roomName)) 'Authorized personal-class video join API'

    Write-Host "`nAll $Passed SkillSwap API smoke tests passed." -ForegroundColor Green
    Write-Host "QA account: $email (created in your local demo database)."
    Write-Host "These tests do not test third-party camera/microphone, actual bank payments, or browser UI."
} catch {
    Write-Host "`nSmoke test stopped after $Passed passes." -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host 'Check that Spring Boot is running on port 8080 with DEMO_MODE=true.'
    exit 1
}
