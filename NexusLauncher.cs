using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

namespace NexusArena
{
    static class Program
    {
        [STAThread]
        static void Main()
        {
            string baseDir = AppDomain.CurrentDomain.BaseDirectory;

            // Look for executable fat JAR
            string jarPath = Path.Combine(baseDir, "NexusArena.jar");
            if (!File.Exists(jarPath))
            {
                jarPath = Path.Combine(baseDir, "desktop-1.0.0.jar");
            }
            if (!File.Exists(jarPath))
            {
                jarPath = Path.Combine(baseDir, "desktop", "target", "desktop-1.0.0.jar");
            }

            if (!File.Exists(jarPath))
            {
                MessageBox.Show("Could not find NexusArena.jar in:\n" + baseDir,
                    "Nexus Arena - Executable Not Found", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            // Locate Java 21 / javaw.exe
            string javaExe = FindJava();
            if (string.IsNullOrEmpty(javaExe))
            {
                DialogResult res = MessageBox.Show(
                    "Java 21 (Eclipse Temurin JDK) is required to run Nexus Arena.\n\n" +
                    "Would you like to open the official Adoptium Temurin download page now?",
                    "Nexus Arena - Java 21 Required", MessageBoxButtons.YesNo, MessageBoxIcon.Warning);
                if (res == DialogResult.Yes)
                {
                    Process.Start("https://adoptium.net/temurin/releases/?version=21");
                }
                return;
            }

            ProcessStartInfo psi = new ProcessStartInfo();
            psi.FileName = javaExe;
            psi.Arguments = "-jar \"" + jarPath + "\"";
            psi.WorkingDirectory = baseDir;
            psi.UseShellExecute = false;
            psi.CreateNoWindow = true;

            try
            {
                Process.Start(psi);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Failed to launch Nexus Arena:\n" + ex.Message,
                    "Launch Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        static string FindJava()
        {
            // 1. Check known Temurin path
            string temurin = @"C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\javaw.exe";
            if (File.Exists(temurin)) return temurin;

            temurin = @"C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe";
            if (File.Exists(temurin)) return temurin;

            // 2. Check JAVA_HOME
            string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
            if (!string.IsNullOrEmpty(javaHome))
            {
                string p = Path.Combine(javaHome, "bin", "javaw.exe");
                if (File.Exists(p)) return p;
                p = Path.Combine(javaHome, "bin", "java.exe");
                if (File.Exists(p)) return p;
            }

            // 3. Search PATH for javaw.exe or java.exe
            string pathVar = Environment.GetEnvironmentVariable("PATH");
            if (!string.IsNullOrEmpty(pathVar))
            {
                string[] dirs = pathVar.Split(';');
                foreach (string dir in dirs)
                {
                    string cleanDir = dir.Trim();
                    if (string.IsNullOrEmpty(cleanDir)) continue;
                    try
                    {
                        string testJavaw = Path.Combine(cleanDir, "javaw.exe");
                        if (File.Exists(testJavaw)) return testJavaw;

                        string testJava = Path.Combine(cleanDir, "java.exe");
                        if (File.Exists(testJava)) return testJava;
                    }
                    catch { }
                }
            }

            // 4. Check Program Files standard folders
            string pf = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
            string adoptiumDir = Path.Combine(pf, "Eclipse Adoptium");
            if (Directory.Exists(adoptiumDir))
            {
                foreach (string sub in Directory.GetDirectories(adoptiumDir))
                {
                    string jw = Path.Combine(sub, "bin", "javaw.exe");
                    if (File.Exists(jw)) return jw;
                }
            }

            string javaDir = Path.Combine(pf, "Java");
            if (Directory.Exists(javaDir))
            {
                foreach (string sub in Directory.GetDirectories(javaDir))
                {
                    string jw = Path.Combine(sub, "bin", "javaw.exe");
                    if (File.Exists(jw)) return jw;
                }
            }

            return null;
        }
    }
}
