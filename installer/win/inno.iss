; Demonstrates copying 3 files and creating an icon.

; SEE THE DOCUMENTATION FOR DETAILS ON CREATING .ISS SCRIPT FILES!

[Setup]
AppName=Document Seeker
AppVersion=1.0
WizardStyle=modern
DefaultDirName={autopf}\DocumentSeeker
DefaultGroupName=DocumentSeeker
UninstallDisplayIcon={app}\nerd.ico
Compression=lzma2
SolidCompression=yes
OutputDir=userdocs:DocumentSeeker

[Files]
Source: "../../startexe/Release/start.exe"; DestDir: "{app}"
Source: "saveToHome"; DestDir: "{app}"
Source: "../../target/lib/docseeker.jar"; DestDir: "{app}"
Source: "../../target/dependency/*"; DestDir: "{app}/dependency"; Flags: ignoreversion recursesubdirs
Source: "../../target/jre/*"; DestDir: "{app}/jre"; Flags: ignoreversion recursesubdirs
Source: "../../tesseract/win/*"; DestDir: "{app}/tesseract"; Flags: ignoreversion recursesubdirs
Source: "vc_redist.x86.exe"; DestDir: {tmp}; Flags: deleteafterinstall

[Run]
Filename: {tmp}\vc_redist.x86.exe; \
    Parameters: "/q /passive /Q:a /c:""msiexec /q /i vcredist.msi"""; \
    StatusMsg: "Installing VC++ 2015 Redistributables..."

[Icons]
Name: "{group}\DocumentSeeker"; Filename : "{app}/start.exe"; WorkingDir: "{app}";