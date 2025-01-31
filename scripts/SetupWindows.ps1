if ($(Split-Path -Path (Get-Location) -Leaf) -eq "scripts" ) {
  Set-Location ..
}

Remove-Item -Recurse -Force "tmp" -ErrorAction SilentlyContinue | Out-Null
New-Item -ItemType Directory -Force -Path "tmp"

Write-Output "Getting latest Tachidesk build files"
#$zipball = (Invoke-WebRequest -Uri "https://api.github.com/repos/Suwayomi/Tachidesk/releases/latest" -UseBasicParsing).content | Select-String -Pattern 'https[\.:\/A-Za-z0-9]*zipball\/[a-zA-Z0-9.]*' -CaseSensitive

#Invoke-WebRequest -Uri $zipball.Matches.Value -OutFile tmp/Tachidesk.zip -UseBasicParsing

Invoke-WebRequest -Uri "https://github.com/Suwayomi/Tachidesk/archive/refs/tags/v0.4.3.zip" -OutFile tmp/Tachidesk.zip -UseBasicParsing

Expand-Archive -Path "tmp/Tachidesk.zip" -DestinationPath "tmp"

$tachidesk_folder = Get-ChildItem -Path "tmp" | Where-Object {$_.Name -match ".*Tachidesk-[a-z0-9\.]*"} | Select-Object FullName

Push-Location $tachidesk_folder.FullName

Write-Output "Setting up android.jar"
&"./AndroidCompat/getAndroid.ps1"

Write-Output "Writing ci gradle.properties"
if (!(Test-Path -Path ".gradle")) {
  New-Item -ItemType Directory -Force -Path ".gradle" -ErrorAction SilentlyContinue
}
Copy-Item ".github/runner-files/ci-gradle.properties" ".gradle/gradle.properties" -Force

Write-Output "Building Tachidesk.jar"
&"./gradlew" :server:shadowJar -x :webUI:copyBuild

$tachidesk_jar = $(Get-ChildItem "server/build" | Where-Object { $_.Name -match '.*\.jar' })[0].FullName

Pop-Location

Write-Output "Copying Tachidesk.jar to resources folder..."
Move-Item -Force $tachidesk_jar "src/main/resources/Tachidesk.jar" -ErrorAction SilentlyContinue

Write-Output "Cleaning up..."
Remove-Item -Recurse -Force "tmp" -ErrorAction SilentlyContinue | Out-Null

Write-Output "Done!"
