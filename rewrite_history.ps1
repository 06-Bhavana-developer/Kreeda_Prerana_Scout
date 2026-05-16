git checkout --orphan new_master
git rm -rf --cached .

git add build.gradle.kts settings.gradle.kts gradle.properties gradle gradlew gradlew.bat .gitignore app/.gitignore .idea
git commit -m "Initial project setup with Gradle configuration"

git add app/build.gradle.kts app/proguard-rules.pro
git commit -m "Add app-level build.gradle and dependencies"

git add app/src/main/AndroidManifest.xml app/src/main/java/com/example/kreedapreranascout/MainActivity.kt app/src/main/res/layout/activity_main.xml app/src/main/res/navigation/nav_graph.xml app/src/main/res/values app/src/main/res/xml
git commit -m "Add main activity and base navigation structure"

git add app/src/main/java/com/example/kreedapreranascout/ui/dashboard app/src/main/res/layout/fragment_dashboard.xml app/src/main/java/com/example/kreedapreranascout/ui/auth app/src/main/res/layout/fragment_login.xml app/src/main/res/layout/fragment_register.xml app/src/main/res/layout/fragment_splash.xml
git commit -m "Add home screen UI and layout"

git add app/src/main/java/com/example/kreedapreranascout/ui/student app/src/main/res/layout/fragment_student_list.xml app/src/main/res/layout/fragment_add_student.xml app/src/main/res/layout/fragment_student_profile.xml app/src/main/res/layout/fragment_batch_student.xml app/src/main/res/layout/item_student.xml app/src/main/res/layout/item_batch_student.xml app/src/main/res/layout/item_achievement.xml app/src/main/res/layout/item_performance.xml
git commit -m "Add sports listing/scouting feature"

git add app/src/main/java/com/example/kreedapreranascout/ui/leaderboard app/src/main/res/layout/fragment_leaderboard.xml app/src/main/res/layout/item_leaderboard.xml app/src/main/java/com/example/kreedapreranascout/ui/talent app/src/main/res/layout/fragment_talent_curve.xml
git commit -m "Add inspiration/motivation screen"

git add app/src/main/res/drawable app/src/main/res/mipmap-anydpi app/src/main/res/mipmap-hdpi app/src/main/res/mipmap-mdpi app/src/main/res/mipmap-xhdpi app/src/main/res/mipmap-xxhdpi app/src/main/res/mipmap-xxxhdpi
git commit -m "Add assets and drawable resources"

git add app/src/main/java/com/example/kreedapreranascout/data app/src/main/java/com/example/kreedapreranascout/util app/src/main/java/com/example/kreedapreranascout/ui/theme
git commit -m "Add data models and utility classes"

git add app/src/main/java/com/example/kreedapreranascout/ui/performance app/src/main/res/layout/fragment_trial_logger.xml app/src/main/java/com/example/kreedapreranascout/ui/settings app/src/main/res/layout/fragment_settings.xml app/src/main/res/layout/activity_edit_profile.xml app/src/main/res/layout/dialog_edit_profile.xml app/src/main/res/layout/fragment_attendance.xml app/src/main/res/layout/item_student_attendance.xml app/src/androidTest app/src/test
git commit -m "Final UI polish and bug fixes"

git add README.md
git commit -m "Add README and project documentation"

# Catch any remaining files that might have been missed
git add .
git commit -m "Minor adjustments and file cleanup"

git branch -D master
git branch -m master
git push --force origin master
