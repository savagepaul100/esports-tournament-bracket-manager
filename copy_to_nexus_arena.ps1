$dest = "C:\Users\PAUL\Downloads\Nexus Arena"
Write-Host "Populating $dest with full source code and compiled path..."

# 1. Compiled Executables & Launchers
Copy-Item "NexusArena.exe" -Destination $dest -Force
Copy-Item "NexusArena.jar" -Destination $dest -Force
Copy-Item "NexusArena.bat" -Destination $dest -Force
Copy-Item "start-desktop.bat" -Destination $dest -Force
Copy-Item "firebase-service-account.json" -Destination $dest -Force
Copy-Item "README.txt" -Destination $dest -Force
Copy-Item "app-logo.ico" -Destination $dest -Force
Copy-Item "app-logo.png" -Destination $dest -Force
Copy-Item "srm_logo.png" -Destination $dest -Force

if (Test-Path "public-web\download\NexusArena-Organizer-Windows.zip") {
    Copy-Item "public-web\download\NexusArena-Organizer-Windows.zip" -Destination $dest -Force
}

# 2. Project Reports and Presentations (PDFs + Scripts)
Copy-Item "NexusArena_Project_Report.pdf" -Destination $dest -Force
Copy-Item "NexusArena_Presentation.pdf" -Destination $dest -Force
Copy-Item "NexusArena_Presentation_Script_and_Roles.docx" -Destination $dest -Force
Copy-Item "NexusArena_Presentation_Script_and_Roles.pdf" -Destination $dest -Force

# 3. Source Code Root & Build Files
Copy-Item "pom.xml" -Destination $dest -Force
Copy-Item "mvnw" -Destination $dest -Force
Copy-Item "mvnw.cmd" -Destination $dest -Force
Copy-Item "firebase.json" -Destination $dest -Force
Copy-Item "firestore.rules" -Destination $dest -Force

# 4. Source Tree: desktop/src
if (!(Test-Path "$dest\desktop\src")) {
    New-Item -ItemType Directory -Path "$dest\desktop\src" -Force | Out-Null
}
Copy-Item -Recurse "desktop\src\*" -Destination "$dest\desktop\src" -Force
Copy-Item "desktop\pom.xml" -Destination "$dest\desktop\pom.xml" -Force

# 5. Compiled Target Path: desktop/target
if (!(Test-Path "$dest\desktop\target")) {
    New-Item -ItemType Directory -Path "$dest\desktop\target" -Force | Out-Null
}
Copy-Item "desktop\target\desktop-1.0.0.jar" -Destination "$dest\desktop\target\desktop-1.0.0.jar" -Force

# 6. Source Tree: public-web
if (!(Test-Path "$dest\public-web")) {
    New-Item -ItemType Directory -Path "$dest\public-web" -Force | Out-Null
}
Copy-Item -Recurse "public-web\*" -Destination "$dest\public-web" -Force

# 7. Maven wrapper directory .mvn
if (!(Test-Path "$dest\.mvn")) {
    New-Item -ItemType Directory -Path "$dest\.mvn" -Force | Out-Null
}
Copy-Item -Recurse ".mvn\*" -Destination "$dest\.mvn" -Force

Write-Host "SUCCESS: C:\Users\PAUL\Downloads\Nexus Arena is now fully populated!"
