import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

def create_script_document():
    doc = Document()
    
    # Page setup - 0.7 inch margins
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(0.65)
        section.bottom_margin = Inches(0.65)
        section.left_margin = Inches(0.7)
        section.right_margin = Inches(0.7)

    # Styles
    C_PRIMARY = RGBColor(0x99, 0x00, 0x00)   # SRM Crimson Red
    C_SECONDARY = RGBColor(0x00, 0x33, 0x66) # Deep Navy
    C_TEXT = RGBColor(0x22, 0x22, 0x22)      # Charcoal
    C_MUTED = RGBColor(0x55, 0x55, 0x55)     # Muted gray
    HEX_LIGHT_BG = "F4F6F9"

    # Header / Title Block
    p_inst = doc.add_paragraph()
    p_inst.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_inst.paragraph_format.space_after = Pt(2)
    r = p_inst.add_run("SRM UNIVERSITY – AP, ANDHRA PRADESH\n")
    r.font.name = "Arial"
    r.font.size = Pt(13)
    r.font.bold = True
    r.font.color.rgb = C_PRIMARY

    r = p_inst.add_run("Department of Computer Science and Engineering | CSE 213: Advanced Java Programming\n")
    r.font.name = "Arial"
    r.font.size = Pt(10)
    r.font.bold = True
    r.font.color.rgb = C_SECONDARY

    r = p_inst.add_run("Faculty Guide: Dr. Srikanth Lukka, Assistant Professor\n")
    r.font.name = "Arial"
    r.font.size = Pt(9.5)
    r.font.italic = True
    r.font.color.rgb = C_MUTED

    # Title Banner
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(8)
    p_title.paragraph_format.space_after = Pt(4)
    r_title = p_title.add_run("PROJECT PRESENTATION SCRIPT & TECHNICAL ROLES HANDBOOK")
    r_title.font.name = "Arial Black"
    r_title.font.size = Pt(15.5)
    r_title.font.bold = True
    r_title.font.color.rgb = C_PRIMARY

    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_after = Pt(12)
    r_sub = p_sub.add_run("Project: NexusArena – Enterprise Dual-Client Esports Platform (Java-Centric Full Stack)")
    r_sub.font.name = "Arial"
    r_sub.font.size = Pt(11)
    r_sub.font.bold = True
    r_sub.font.color.rgb = C_MUTED

    # Horizontal line
    p_hr = doc.add_paragraph()
    p_hr.paragraph_format.space_after = Pt(10)
    p_hr_border = parse_xml(f'<w:pBdr {nsdecls("w")}><w:bottom w:val="single" w:sz="12" w:space="1" w:color="990000"/></w:pBdr>')
    p_hr._p.get_or_add_pPr().append(p_hr_border)

    # -------------------------------------------------------------
    # SECTION 1: BALANCED ROLES TABLE (JAVA PRIMARY + COMPLEMENTARY TECH)
    # -------------------------------------------------------------
    h1 = doc.add_heading(level=1)
    h1.paragraph_format.space_before = Pt(8)
    h1.paragraph_format.space_after = Pt(6)
    r = h1.add_run("1. Team Members & Technical Specialization Matrix")
    r.font.name = "Arial"
    r.font.size = Pt(12.5)
    r.font.bold = True
    r.font.color.rgb = C_PRIMARY

    p_note = doc.add_paragraph()
    p_note.paragraph_format.space_after = Pt(6)
    r = p_note.add_run("Note: Every team member developed core Java modules for the primary desktop engine while also handling integrated cloud, web, and UI subsystems to deliver an enterprise-grade platform.")
    r.font.name = "Arial"
    r.font.size = Pt(9)
    r.font.italic = True
    r.font.color.rgb = C_MUTED

    table = doc.add_table(rows=4, cols=4)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False

    headers = ["Member Name", "Registration No.", "Designated Role", "Primary Java Modules & Complementary Technologies"]
    col_widths = [Inches(1.5), Inches(1.3), Inches(2.0), Inches(2.4)]

    # Format header row
    hdr_cells = table.rows[0].cells
    for i, title in enumerate(headers):
        hdr_cells[i].text = title
        hdr_cells[i].width = col_widths[i]
        shading = parse_xml(f'<w:shd {nsdecls("w")} w:fill="003366"/>')
        hdr_cells[i]._tc.get_or_add_tcPr().append(shading)
        for p in hdr_cells[i].paragraphs:
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            for run in p.runs:
                run.font.name = "Arial"
                run.font.size = Pt(9.5)
                run.font.bold = True
                run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

    data = [
        ("P. Abhishek Paul", "AP25110071316", "Lead Systems Architect & Java Desktop Engineer", 
         "• Primary Java: JavaFX 21 GUI, Event Dispatch Thread (EDT) architecture, Asynchronous Concurrency (CompletableFuture, Platform.runLater), Maven Shade build pipeline.\n• Complementary: Industrial UI/UX design tokens, desktop release packaging, auto-update check daemon."),
        ("K. Nithien", "AP25110070134", "Java Cloud Integration & Backend Systems Engineer", 
         "• Primary Java: Java 21 HTTP Client (java.net.http), non-blocking REST CRUD engine, Google Gson serialization/deserialization adapters, SSE cloud stream listeners.\n• Complementary: Google Firebase Realtime Database schema, Cloud NoSQL data rules, cloud endpoints configuration."),
        ("Shaik Hasnain", "AP25110070228", "Java Core Logic & Web Companion Developer", 
         "• Primary Java: Java Collections Framework, Single-Elimination & Round-Robin bracket matchmaking algorithms, Match state machines, SHA-256 Tournament Master Key security.\n• Complementary: Web Companion Portal (nexusarena.web.app using HTML5/CSS3/JS), live web bracket ticker, cross-platform UI sync.")
    ]

    for row_idx, row_data in enumerate(data, start=1):
        row_cells = table.rows[row_idx].cells
        bg_color = "FFFFFF" if row_idx % 2 != 0 else HEX_LIGHT_BG
        for col_idx, text in enumerate(row_data):
            row_cells[col_idx].text = text
            row_cells[col_idx].width = col_widths[col_idx]
            shading = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{bg_color}"/>')
            row_cells[col_idx]._tc.get_or_add_tcPr().append(shading)
            for p in row_cells[col_idx].paragraphs:
                p.paragraph_format.space_before = Pt(3)
                p.paragraph_format.space_after = Pt(3)
                for run in p.runs:
                    run.font.name = "Arial"
                    run.font.size = Pt(8.8)
                    run.font.color.rgb = C_TEXT
                    if col_idx == 0:
                        run.font.bold = True

    # -------------------------------------------------------------
    # SECTION 2: ARCHITECTURE & TECH STACK
    # -------------------------------------------------------------
    h2 = doc.add_heading(level=1)
    h2.paragraph_format.space_before = Pt(12)
    h2.paragraph_format.space_after = Pt(6)
    r = h2.add_run("2. Technology Stack Architecture (Java-Centric Dual-Client)")
    r.font.name = "Arial"
    r.font.size = Pt(12.5)
    r.font.bold = True
    r.font.color.rgb = C_PRIMARY

    p_tech = doc.add_paragraph()
    p_tech.paragraph_format.space_after = Pt(4)
    r = p_tech.add_run("NexusArena is architected with Java as the primary backbone, driving native desktop execution, multithreading, and cloud communication, augmented by cloud and web technologies for real-time ubiquity:\n")
    r.font.name = "Arial"
    r.font.size = Pt(9.5)
    r.font.color.rgb = C_TEXT

    tech_points = [
        ("Core Language (Java 21 LTS - Dominant): ", "Powers the entire desktop client, business logic, asynchronous worker threads, cryptographic security, and HTTP networking."),
        ("Desktop GUI Engine (JavaFX 21): ", "Rich client UI utilizing Scene Graph, BorderPane/VBox layouts, dynamic TableViews, and custom JavaFX CSS skinning (#0D0D11 industrial esports palette)."),
        ("Asynchronous Concurrency (Java Concurrency API): ", "CompletableFuture<T> and Platform.runLater() eliminate UI freezes by delegating all cloud I/O off the JavaFX Event Dispatch Thread."),
        ("Network & Serialization (java.net.http + Google Gson): ", "Pure Java HTTP/2 asynchronous REST client communicating directly with cloud endpoints, serializing models into structured JSON trees."),
        ("Cloud Infrastructure (Google Firebase Realtime DB): ", "Centralized NoSQL JSON cloud database ensuring zero local-database isolation, sharing live tournament data between desktop and web."),
        ("Web Companion (HTML5 / Modern JavaScript / Firebase Hosting): ", "Lightweight, zero-install companion portal (nexusarena.web.app) mirroring desktop layouts and streaming live brackets for spectators.")
    ]
    for bold_prefix, desc in tech_points:
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_after = Pt(2)
        r_b = p.add_run(bold_prefix)
        r_b.font.name = "Arial"
        r_b.font.size = Pt(9)
        r_b.font.bold = True
        r_b.font.color.rgb = C_SECONDARY
        r_d = p.add_run(desc)
        r_d.font.name = "Arial"
        r_d.font.size = Pt(9)
        r_d.font.color.rgb = C_TEXT

    # -------------------------------------------------------------
    # SECTION 3: STEP-BY-STEP PRESENTATION SCRIPT
    # -------------------------------------------------------------
    h3 = doc.add_heading(level=1)
    h3.paragraph_format.space_before = Pt(12)
    h3.paragraph_format.space_after = Pt(6)
    r = h3.add_run("3. Complete Presentation & Viva Script (Speaker-by-Speaker)")
    r.font.name = "Arial"
    r.font.size = Pt(12.5)
    r.font.bold = True
    r.font.color.rgb = C_PRIMARY

    def add_speaker_block(speaker_name, role_title, timing, slides, script_text, viva_qa):
        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False
        c = tbl.rows[0].cells[0]
        c.width = Inches(7.2)
        shading = parse_xml(f'<w:shd {nsdecls("w")} w:fill="003366"/>')
        c._tc.get_or_add_tcPr().append(shading)
        p = c.paragraphs[0]
        p.paragraph_format.space_before = Pt(3)
        p.paragraph_format.space_after = Pt(3)
        r = p.add_run(f"SPEAKER: {speaker_name.upper()}  |  {role_title.upper()}")
        r.font.name = "Arial"
        r.font.size = Pt(10)
        r.font.bold = True
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

        p_meta = doc.add_paragraph()
        p_meta.paragraph_format.space_before = Pt(3)
        p_meta.paragraph_format.space_after = Pt(3)
        r = p_meta.add_run(f"Target Duration: {timing}   |   Slides Covered: {slides}\n")
        r.font.name = "Arial"
        r.font.size = Pt(9)
        r.font.bold = True
        r.font.color.rgb = C_PRIMARY

        p_shead = doc.add_paragraph()
        p_shead.paragraph_format.space_after = Pt(2)
        r = p_shead.add_run("Oral Presentation Dialogue / Script:")
        r.font.name = "Arial"
        r.font.size = Pt(9.5)
        r.font.bold = True
        r.font.color.rgb = C_SECONDARY

        for para in script_text:
            p = doc.add_paragraph()
            p.paragraph_format.space_after = Pt(3)
            r = p.add_run(f"\"{para}\"")
            r.font.name = "Georgia"
            r.font.size = Pt(9.3)
            r.font.italic = True
            r.font.color.rgb = C_TEXT

        p_vhead = doc.add_paragraph()
        p_vhead.paragraph_format.space_before = Pt(3)
        p_vhead.paragraph_format.space_after = Pt(2)
        r = p_vhead.add_run("Anticipated Faculty Viva Questions & Exact Answers:")
        r.font.name = "Arial"
        r.font.size = Pt(9.5)
        r.font.bold = True
        r.font.color.rgb = C_PRIMARY

        for q, a in viva_qa:
            p_q = doc.add_paragraph()
            p_q.paragraph_format.space_after = Pt(1)
            r = p_q.add_run("Q: " + q)
            r.font.name = "Arial"
            r.font.size = Pt(8.8)
            r.font.bold = True
            r.font.color.rgb = C_SECONDARY

            p_a = doc.add_paragraph()
            p_a.paragraph_format.space_after = Pt(3)
            r = p_a.add_run("Answer: " + a)
            r.font.name = "Arial"
            r.font.size = Pt(8.8)
            r.font.color.rgb = C_TEXT

        doc.add_paragraph().paragraph_format.space_after = Pt(5)

    # Speaker 1: P. Abhishek Paul
    sp1_script = [
        "Respected Dr. Srikanth Lukka sir and members of the evaluation committee, good morning. I am P. Abhishek Paul, presenting on behalf of our team for CSE 213 Advanced Java Programming. Our project is NexusArena, an enterprise esports tournament management platform centered around a high-performance Java architecture.",
        "While many esports tools exist, they either rely on sluggish web apps that lag during live LAN events, or offline desktop tools that cannot synchronize data across organizers. We engineered NexusArena with Java at its absolute core to achieve native desktop responsiveness, backed by real-time cloud synchronization.",
        "My primary engineering contribution was designing the JavaFX Desktop Architecture and solving critical concurrency challenges. In initial builds, executing cloud operations directly on the JavaFX Event Dispatch Thread caused severe UI freezes and crashes. To fix this, I rebuilt our threading model using Java's CompletableFuture and Task utilities.",
        "Now, all network requests run asynchronously in the background. Once data is ready, we safely dispatch updates to the UI using Platform.runLater(). Beyond threading, I architected our modular UI in JavaFX—replacing cluttered top bars with a modern collapsible sidebar navigation and applying a custom JavaFX CSS skin in an industrial dark theme. I also configured our Maven build automation to package the entire application into a self-contained executable with automated version-check capabilities."
    ]
    sp1_viva = [
        ("Why was the JavaFX app freezing initially, and how did your Java code fix it?",
         "JavaFX enforces a single UI thread called the Event Dispatch Thread (EDT). Performing blocking I/O calls on this thread halts rendering and event processing. I moved all network operations into background daemon threads using CompletableFuture.supplyAsync() and used Platform.runLater() to push UI mutations back to the EDT."),
        ("How did you manage styling and responsive layouts in JavaFX?",
         "We used a JavaFX BorderPane root container with nested VBox and GridPane layouts, styled via JavaFX CSS (styles.css). We implemented responsive button states and angular borders using custom CSS classes without adding third-party styling overhead.")
    ]
    add_speaker_block("P. Abhishek Paul", "Lead Systems Architect & Java Desktop Engineer (AP25110071316)", "3 Minutes", "Slides 1 to 4, 11 (Introduction, Concurrency, UI, Demo)", sp1_script, sp1_viva)

    # Speaker 2: K. Nithien
    sp2_script = [
        "Good morning faculty. I am K. Nithien. My primary focus was the Java Cloud Integration and Network I/O subsystem, alongside our cloud database architecture.",
        "Rather than using an isolated local database like SQLite or Derby that confines tournament data to one machine, we architected a zero-local, cloud-native backend. I developed FirebaseService.java as our centralized Java networking engine using Java 21's modern java.net.http.HttpClient.",
        "Using HttpRequest and HttpResponse, I implemented fully non-blocking asynchronous REST methods—GET, POST, PUT, PATCH, and DELETE. This communicates directly with our Google Firebase Realtime Database over HTTP/2, completely avoiding bloated external SDKs.",
        "To serialize and deserialize our data, I integrated Google Gson. I built custom type adapters for our Tournament, Team, Player, and Match Java records, converting complex nested JSON trees into immutable Java models. Furthermore, I implemented Java-based Server-Sent Events (SSE) listeners that continuously receive cloud updates, ensuring the desktop app stays in real-time sync with match scores and bracket advancements."
    ]
    sp2_viva = [
        ("Why choose java.net.http over legacy HttpURLConnection or third-party libraries like OkHttp?",
         "Java 21's java.net.http is built directly into the Java Standard Library, supports HTTP/2 multiplexing, non-blocking asynchronous requests via CompletableFuture, and native reactive streams. It provides enterprise throughput without adding external JAR bloat."),
        ("How did you structure the cloud database to support both Java and real-time streaming?",
         "We designed a normalized JSON tree in Firebase Realtime DB with top-level keys for /tournaments, /teams, /matches, and /leaderboards. Security rules enforce validation while our Java Gson adapters seamlessly deserialize this JSON into strongly-typed Java objects.")
    ]
    add_speaker_block("K. Nithien", "Java Cloud Integration & Backend Systems Engineer (AP25110070134)", "3 Minutes", "Slides 5 to 7 (Backend Architecture, Cloud Sync, Data Serialization)", sp2_script, sp2_viva)

    # Speaker 3: Shaik Hasnain
    sp3_script = [
        "Good morning panel. I am Shaik Hasnain. My domain in NexusArena was Core Java Algorithms and Security, alongside developing our Web Companion portal.",
        "In Java, I engineered TournamentEngine.java and BracketGenerator.java using the Java Collections Framework—specifically utilizing Queues, Lists, and TreeMaps. I implemented the Single Elimination Tournament Bracket algorithm, which dynamically structures tournament trees for 4, 8, 16, or 32 teams, assigns seeds, and automatically advances winners through successive rounds as matches conclude.",
        "I also created the Java Live Match Simulation and Scoring State Machine, which manages live game ticks, player kills, and score progression. For system integrity, I built the Tournament Master Key Security Module in Java. Using cryptographic SHA-256 password hashing, sensitive administrative actions—such as deleting teams or altering official brackets—are verified before cloud updates are permitted.",
        "To complement our Java desktop client, I also developed the Web Companion portal at nexusarena.web.app using HTML5 and modern JavaScript. It connects to the exact same cloud database so spectators can view brackets and live scores on mobile and web in real-time, synchronized with the Java app."
    ]
    sp3_viva = [
        ("What data structures in the Java Collections Framework did you use for bracket generation?",
         "I used a binary-tree-like model backed by an ArrayList of Match nodes, and a Queue<Team> to calculate seeded matchups and manage tournament byes when team counts are not powers of two."),
        ("How do the Java desktop app and web portal stay synchronized?",
         "Both clients share the identical Firebase Realtime Database cloud schema. When the Java desktop application submits an updated match score via java.net.http, Firebase triggers a real-time event that immediately updates both the Java desktop SSE listener and the web portal's WebSocket listener.")
    ]
    add_speaker_block("Shaik Hasnain", "Java Core Logic & Web Companion Developer (AP25110070228)", "3 Minutes", "Slides 8 to 10, 12 (Algorithms, Security, Web Sync, Conclusion)", sp3_script, sp3_viva)

    # -------------------------------------------------------------
    # SECTION 4: LIVE DEMONSTRATION WORKFLOW
    # -------------------------------------------------------------
    h4 = doc.add_heading(level=1)
    h4.paragraph_format.space_before = Pt(12)
    h4.paragraph_format.space_after = Pt(6)
    r = h4.add_run("4. Recommended Live Project Demonstration Workflow (2 Minutes)")
    r.font.name = "Arial"
    r.font.size = Pt(12.5)
    r.font.bold = True
    r.font.color.rgb = C_PRIMARY

    demo_steps = [
        ("Step 1 (Abhishek Paul - Launch & Responsiveness): ", "Launch the native Java desktop app. Point out the zero-lag startup, collapsible sidebar navigation, and the responsive JavaFX CSS esports styling."),
        ("Step 2 (Hasnain - Tournament & Bracket Creation): ", "Create a new 8-team tournament in Java. Set a Master Password. Demonstrate how the Java tournament algorithm instantly generates Quarterfinals, Semifinals, and Finals brackets."),
        ("Step 3 (Nithien & Hasnain - Dual-Client Live Cloud Sync): ", "Open nexusarena.web.app in the browser alongside the Java desktop app. Update a match score in the Java desktop client; show that the web bracket instantly updates live via Firebase without reloading."),
        ("Step 4 (Abhishek & Hasnain - Security Verification): ", "Attempt to delete a team or modify tournament parameters. Enter an incorrect password to show rejection, then enter the correct password to demonstrate our Java SHA-256 authentication.")
    ]

    for step_title, step_desc in demo_steps:
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_after = Pt(3)
        r_t = p.add_run(step_title)
        r_t.font.name = "Arial"
        r_t.font.size = Pt(9.3)
        r_t.font.bold = True
        r_t.font.color.rgb = C_SECONDARY
        r_d = p.add_run(step_desc)
        r_d.font.name = "Arial"
        r_d.font.size = Pt(9.3)
        r_d.font.color.rgb = C_TEXT

    # Save docx
    docx_path = r"C:\Users\PAUL\Downloads\Java Project\NexusArena_Presentation_Script_and_Roles.docx"
    doc.save(docx_path)
    print(f"Generated DOCX: {docx_path}")

create_script_document()
