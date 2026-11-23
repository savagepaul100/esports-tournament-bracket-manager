using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

namespace NexusArenaLauncher
{
    static class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            string baseDir = AppDomain.CurrentDomain.BaseDirectory;
            string jarPath = Path.Combine(baseDir, "NexusArena.jar");

            if (!File.Exists(jarPath))
            {
                jarPath = Path.Combine(baseDir, "desktop", "target", "desktop-1.0.0.jar");
            }
            if (!File.Exists(jarPath))
            {
                jarPath = Path.Combine(baseDir, "desktop-1.0.0.jar");
            }

            if (!File.Exists(jarPath))
            {
                MessageBox.Show(
                    "Could not find NexusArena.jar in:\n" + baseDir + "\n\nPlease ensure NexusArena.jar is present.",
                    "Nexus Arena - Missing Application File",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
                return;
            }

            string javaExe = FindJava();
            if (string.IsNullOrEmpty(javaExe))
            {
                DialogResult res = MessageBox.Show(
                    "Java 21 LTS runtime was not detected on your system.\n\n" +
                    "Nexus Arena requires Java 21 LTS to run.\n" +
                    "Would you like to download Eclipse Adoptium Temurin JDK 21 now?",
                    "Nexus Arena - Java 21 Required",
                    MessageBoxButtons.YesNo,
                    MessageBoxIcon.Warning
                );

                if (res == DialogResult.Yes)
                {
                    try
                    {
                        Process.Start("https://adoptium.net/temurin/releases/?version=21");
                    }
                    catch { }
                }
                return;
            }

            try
            {
                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = javaExe;
                
                string joinedArgs = string.Join(" ", args);
                psi.Arguments = "-jar \"" + jarPath + "\" " + joinedArgs;
                psi.WorkingDirectory = baseDir;
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;

                Process proc = Process.Start(psi);
                if (proc == null)
                {
                    MessageBox.Show("Failed to launch Nexus Arena.", "Launch Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error launching Nexus Arena:\n" + ex.Message, "Launch Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        static string FindJava()
        {
            // 1. Check JAVA_HOME
            string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
            if (!string.IsNullOrEmpty(javaHome))
            {
                string p1 = Path.Combine(javaHome, "bin", "javaw.exe");
                if (File.Exists(p1)) return p1;
                string p2 = Path.Combine(javaHome, "bin", "java.exe");
                if (File.Exists(p2)) return p2;
            }

            // 2. Check PATH
            string pathEnv = Environment.GetEnvironmentVariable("PATH");
            if (!string.IsNullOrEmpty(pathEnv))
            {
                foreach (string rawDir in pathEnv.Split(';'))
                {
                    string dir = rawDir.Trim();
                    if (string.IsNullOrEmpty(dir)) continue;
                    try
                    {
                        string jw = Path.Combine(dir, "javaw.exe");
                        if (File.Exists(jw)) return jw;
                        string je = Path.Combine(dir, "java.exe");
                        if (File.Exists(je)) return je;
                    }
                    catch { }
                }
            }

            // 3. Check Program Files Eclipse Adoptium
            string pf = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
            string adoptiumDir = Path.Combine(pf, "Eclipse Adoptium");
            if (Directory.Exists(adoptiumDir))
            {
                try
                {
                    foreach (string jdk in Directory.GetDirectories(adoptiumDir, "jdk-21*"))
                    {
                        string jw = Path.Combine(jdk, "bin", "javaw.exe");
                        if (File.Exists(jw)) return jw;
                        string je = Path.Combine(jdk, "bin", "java.exe");
                        if (File.Exists(je)) return je;
                    }
                    foreach (string jdk in Directory.GetDirectories(adoptiumDir, "*"))
                    {
                        string jw = Path.Combine(jdk, "bin", "javaw.exe");
                        if (File.Exists(jw)) return jw;
                        string je = Path.Combine(jdk, "bin", "java.exe");
                        if (File.Exists(je)) return je;
                    }
                }
                catch { }
            }

            // 4. Check Program Files Java
            string javaDir = Path.Combine(pf, "Java");
            if (Directory.Exists(javaDir))
            {
                try
                {
                    foreach (string jdk in Directory.GetDirectories(javaDir, "jdk-21*"))
                    {
                        string jw = Path.Combine(jdk, "bin", "javaw.exe");
                        if (File.Exists(jw)) return jw;
                        string je = Path.Combine(jdk, "bin", "java.exe");
                        if (File.Exists(je)) return je;
                    }
                }
                catch { }
            }

            return null;
        }
    }
}
