import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN
from pptx.enum.shapes import MSO_SHAPE

def build_presentation():
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # Theme colors
    COLOR_BG = RGBColor(12, 12, 14)       # #0c0c0e
    COLOR_CARD = RGBColor(24, 24, 27)     # #18181b
    COLOR_CARD_BORDER = RGBColor(39, 39, 42) # #27272a
    COLOR_RED = RGBColor(220, 38, 38)     # #dc2626
    COLOR_ORANGE = RGBColor(234, 88, 12)  # #ea580c
    COLOR_WHITE = RGBColor(255, 255, 255)
    COLOR_GRAY = RGBColor(161, 161, 170)  # #a1a1aa
    COLOR_MUTED = RGBColor(113, 113, 122) # #71717a

    def apply_bg(slide):
        bg = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height)
        bg.fill.solid()
        bg.fill.fore_color.rgb = COLOR_BG
        bg.line.fill.background()
        return bg

    def add_header(slide, title_text, category_text="CSE 213 • ADVANCED JAVA PROGRAMMING"):
        # Header banner line
        line = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(0.45), Inches(0.08), Inches(0.85))
        line.fill.solid()
        line.fill.fore_color.rgb = COLOR_RED
        line.line.fill.background()

        # Category
        cat_box = slide.shapes.add_textbox(Inches(1.0), Inches(0.38), Inches(10), Inches(0.3))
        tf_cat = cat_box.text_frame
        tf_cat.word_wrap = True
        p_cat = tf_cat.paragraphs[0]
        p_cat.text = category_text.upper()
        p_cat.font.name = 'Trebuchet MS'
        p_cat.font.size = Pt(11)
        p_cat.font.bold = True
        p_cat.font.color.rgb = COLOR_ORANGE

        # Title
        title_box = slide.shapes.add_textbox(Inches(1.0), Inches(0.65), Inches(11), Inches(0.65))
        tf_title = title_box.text_frame
        tf_title.word_wrap = True
        p_title = tf_title.paragraphs[0]
        p_title.text = title_text
        p_title.font.name = 'Trebuchet MS'
        p_title.font.size = Pt(24)
        p_title.font.bold = True
        p_title.font.color.rgb = COLOR_WHITE

    def add_card(slide, left, top, width, height, title="", border_color=COLOR_CARD_BORDER):
        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        card.fill.solid()
        card.fill.fore_color.rgb = COLOR_CARD
        card.line.color.rgb = border_color
        card.line.width = Pt(1.5)

        if title:
            tb = slide.shapes.add_textbox(left + Inches(0.2), top + Inches(0.15), width - Inches(0.4), Inches(0.4))
            p = tb.text_frame.paragraphs[0]
            p.text = title
            p.font.name = 'Trebuchet MS'
            p.font.size = Pt(14)
            p.font.bold = True
            p.font.color.rgb = COLOR_ORANGE

        return card

    # =========================================================
    # SLIDE 1: TITLE SLIDE
    # =========================================================
    slide1 = prs.slides.add_slide(blank_layout)
    apply_bg(slide1)

    # Accent top border
    top_bar = slide1.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, Inches(0.1))
    top_bar.fill.solid()
    top_bar.fill.fore_color.rgb = COLOR_RED
    top_bar.line.fill.background()

    # Logo (SRM University Emblem left, Nexus Arena Product Logo right)
    if os.path.exists("srm_logo.png"):
        slide1.shapes.add_picture("srm_logo.png", Inches(0.8), Inches(0.8), width=Inches(1.5))
    if os.path.exists("app-logo.png"):
        slide1.shapes.add_picture("app-logo.png", Inches(11.0), Inches(0.8), width=Inches(1.5))

    # University header
    tb = slide1.shapes.add_textbox(Inches(2.5), Inches(0.85), Inches(10), Inches(1.2))
    tf = tb.text_frame
    p1 = tf.paragraphs[0]
    p1.text = "SRM UNIVERSITY – AP, ANDHRA PRADESH"
    p1.font.name = 'Trebuchet MS'
    p1.font.size = Pt(18)
    p1.font.bold = True
    p1.font.color.rgb = COLOR_WHITE

    p2 = tf.add_paragraph()
    p2.text = "Department of Computer Science & Engineering • School of Engineering and Sciences"
    p2.font.name = 'Arial'
    p2.font.size = Pt(12)
    p2.font.color.rgb = COLOR_GRAY

    # Main Project Title Box
    card_title = add_card(slide1, Inches(0.8), Inches(2.2), Inches(11.73), Inches(2.2), border_color=COLOR_RED)
    tb_title = slide1.shapes.add_textbox(Inches(1.1), Inches(2.35), Inches(11.1), Inches(1.8))
    tframe = tb_title.text_frame
    tframe.word_wrap = True

    pt1 = tframe.paragraphs[0]
    pt1.text = "NEXUS ARENA"
    pt1.font.name = 'Trebuchet MS'
    pt1.font.size = Pt(36)
    pt1.font.bold = True
    pt1.font.color.rgb = COLOR_WHITE

    pt2 = tframe.add_paragraph()
    pt2.text = "Cloud-Synchronized E-Sports Tournament & Bracket Management System"
    pt2.font.name = 'Trebuchet MS'
    pt2.font.size = Pt(18)
    pt2.font.bold = True
    pt2.font.color.rgb = COLOR_ORANGE

    pt3 = tframe.add_paragraph()
    pt3.text = "CSE 213 – Advanced Java Programming • III Semester B.Tech Project Evaluation [Section: CSE AI ML F]"
    pt3.font.name = 'Arial'
    pt3.font.size = Pt(12.5)
    pt3.font.color.rgb = COLOR_GRAY

    # Team Members Card
    add_card(slide1, Inches(0.8), Inches(4.7), Inches(7.5), Inches(2.3), title="STUDENT PROJECT TEAM")
    tb_team = slide1.shapes.add_textbox(Inches(1.0), Inches(5.25), Inches(7.1), Inches(1.6))
    tframe_team = tb_team.text_frame
    
    team_members = [
        ("P. Abhishek Paul", "AP25110071316"),
        ("K. Nithien", "AP25110070134"),
        ("Shaik Hasnain", "AP25110070228")
    ]
    for i, (name, reg) in enumerate(team_members):
        p = tframe_team.paragraphs[0] if i == 0 else tframe_team.add_paragraph()
        p.text = f"•  {name}   —   Reg No: {reg}   (CSE AI ML F)"
        p.font.name = 'Arial'
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = COLOR_WHITE
        p.space_after = Pt(6)

    # Faculty Guide Card
    add_card(slide1, Inches(8.5), Inches(4.7), Inches(4.03), Inches(2.3), title="FACULTY MENTOR")
    tb_guide = slide1.shapes.add_textbox(Inches(8.7), Inches(5.25), Inches(3.6), Inches(1.6))
    tf_guide = tb_guide.text_frame
    
    pg1 = tf_guide.paragraphs[0]
    pg1.text = "Dr. Srikanth Lukka"
    pg1.font.name = 'Trebuchet MS'
    pg1.font.size = Pt(16)
    pg1.font.bold = True
    pg1.font.color.rgb = COLOR_WHITE

    pg2 = tf_guide.add_paragraph()
    pg2.text = "Assistant Professor\nDepartment of CSE\nSRM University – AP"
    pg2.font.name = 'Arial'
    pg2.font.size = Pt(12)
    pg2.font.color.rgb = COLOR_GRAY

    # =========================================================
    # SLIDE 2: PROBLEM STATEMENT & MOTIVATION
    # =========================================================
    slide2 = prs.slides.add_slide(blank_layout)
    apply_bg(slide2)
    add_header(slide2, "Problem Statement & Practical Motivation", "01 • BACKGROUND & PROBLEM DEFINITION")

    cards_s2 = [
        ("Manual Bracket Latency", 
         "Spreadsheets & handwritten charts cause 30–60 min delays calculating power-of-two pairings and BYE progressions between rounds during university fests."),
        ("Score Recording Inconsistencies",
         "Discrepancies in fast-paced competitive rounds and player kills between opposing captains lead to contested results and tournament stalls."),
        ("No Real-Time Cloud Sync",
         "Audience, participants, and remote spectators cannot track live brackets, resulting in crowded referee desks and venue miscoordination."),
        ("Security & Result Tampering Risks",
         "Lack of cryptographic access controls allows unauthorized score alterations or disputes without verified audit logging.")
    ]

    for idx, (head, text) in enumerate(cards_s2):
        row = idx // 2
        col = idx % 2
        l = Inches(0.8 + col * 5.95)
        t = Inches(1.6 + row * 2.7)
        add_card(slide2, l, t, Inches(5.75), Inches(2.45), title=head, border_color=COLOR_RED if idx == 0 else COLOR_CARD_BORDER)
        
        tb = slide2.shapes.add_textbox(l + Inches(0.2), t + Inches(0.65), Inches(5.35), Inches(1.6))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = text
        p.font.name = 'Arial'
        p.font.size = Pt(13)
        p.font.color.rgb = COLOR_GRAY

    # =========================================================
    # SLIDE 3: PROPOSED SOLUTION - NEXUS ARENA OVERVIEW
    # =========================================================
    slide3 = prs.slides.add_slide(blank_layout)
    apply_bg(slide3)
    add_header(slide3, "Proposed Solution: Nexus Arena Overview", "02 • SOLUTION ARCHITECTURE")

    add_card(slide3, Inches(0.8), Inches(1.6), Inches(5.75), Inches(5.3), title="NATIVE JAVAFX 21 DESKTOP CLIENT", border_color=COLOR_ORANGE)
    tb_left = slide3.shapes.add_textbox(Inches(1.0), Inches(2.2), Inches(5.35), Inches(4.5))
    tf_l = tb_left.text_frame
    tf_l.word_wrap = True
    
    features_desktop = [
        "Industrial Organizer Command Center with dark cyberpunk gamer CSS theme.",
        "Zero-Lag (0ms) Responsiveness: Local in-memory caching guarantees zero UI freeze.",
        "Automated Single-Elimination Engine: Power-of-2 balancing and winner propagation.",
        "Tournament-Level Master Security: Single session password protects brackets & rosters.",
        "Standalone Windows Native Executable (NexusArena.exe) without batch scripts."
    ]
    for i, f in enumerate(features_desktop):
        p = tf_l.paragraphs[0] if i == 0 else tf_l.add_paragraph()
        p.text = "✓  " + f
        p.font.name = 'Arial'
        p.font.size = Pt(12.5)
        p.font.color.rgb = COLOR_WHITE
        p.space_after = Pt(10)

    add_card(slide3, Inches(6.75), Inches(1.6), Inches(5.78), Inches(5.3), title="REACTIVE CLOUD WEB PORTAL", border_color=COLOR_RED)
    tb_right = slide3.shapes.add_textbox(Inches(6.95), Inches(2.2), Inches(5.35), Inches(4.5))
    tf_r = tb_right.text_frame
    tf_r.word_wrap = True
    
    features_web = [
        "Public Live Dashboard: Hosted live on Google Cloud (nexusarena-esports.web.app).",
        "Bidirectional Firestore Synchronization: Real-time onSnapshot match score updates.",
        "Multi-Game Pre-Seeded Datasets: Immediate first-render for CS2, Valorant, Dota 2, Apex, BGMI, and Rocket League.",
        "Public Roster Inspection: Verified team gamer tags, captain emails, and seeding ladder.",
        "Integrated In-App Auto-Updater: Direct cloud queries to version.json for 1-click updates."
    ]
    for i, f in enumerate(features_web):
        p = tf_r.paragraphs[0] if i == 0 else tf_r.add_paragraph()
        p.text = "✓  " + f
        p.font.name = 'Arial'
        p.font.size = Pt(12.5)
        p.font.color.rgb = COLOR_WHITE
        p.space_after = Pt(10)

    # =========================================================
    # SLIDE 4: SYSTEM ARCHITECTURE & DATA FLOW
    # =========================================================
    slide4 = prs.slides.add_slide(blank_layout)
    apply_bg(slide4)
    add_header(slide4, "System Architecture & Asynchronous Concurrency", "03 • ARCHITECTURAL DESIGN")

    # Flow Diagram Cards
    flow_steps = [
        ("1. JavaFX 21 UI Layer", "TournamentsView, BracketView, MatchesView, TeamsView", Inches(0.8)),
        ("2. High-Speed Cache", "CopyOnWriteArrayList & ConcurrentHashMap (0ms latency)", Inches(3.8)),
        ("3. Daemon Worker Pool", "ExecutorService cachedThreadPool (Async Cloud Sync)", Inches(6.8)),
        ("4. Google Cloud Firestore", "Central authoritative database (nexusarena-esports)", Inches(9.8))
    ]

    for title, desc, left in flow_steps:
        add_card(slide4, left, Inches(1.6), Inches(2.73), Inches(2.4), title=title, border_color=COLOR_ORANGE)
        tb = slide4.shapes.add_textbox(left + Inches(0.15), Inches(2.2), Inches(2.43), Inches(1.6))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = desc
        p.font.name = 'Arial'
        p.font.size = Pt(11.5)
        p.font.color.rgb = COLOR_GRAY

    # Technical Explanation Card
    add_card(slide4, Inches(0.8), Inches(4.3), Inches(11.73), Inches(2.6), title="ASYNC CONCURRENCY & ZERO-LAG DESIGN HIGHLIGHTS")
    tb_desc = slide4.shapes.add_textbox(Inches(1.0), Inches(4.85), Inches(11.3), Inches(1.8))
    tf_d = tb_desc.text_frame
    tf_d.word_wrap = True

    highlights = [
        "Decoupled Network Threading: All database writes and queries are offloaded to background daemon threads, preventing JavaFX UI Application Thread starvation and eliminating 2-3s button lag.",
        "Local-First Guarantee: Read operations execute against in-memory concurrent structures in 0.001ms, while mutations update local state instantly and sync to cloud in background.",
        "Reactive Bidirectional Sync: Google Cloud Firestore triggers real-time snapshot listeners on both Web and Desktop clients simultaneously when scores are finalized."
    ]
    for i, h in enumerate(highlights):
        p = tf_d.paragraphs[0] if i == 0 else tf_d.add_paragraph()
        p.text = "•  " + h
        p.font.name = 'Arial'
        p.font.size = Pt(12)
        p.font.color.rgb = COLOR_WHITE
        p.space_after = Pt(6)

    # =========================================================
    # SLIDE 5: ADVANCED JAVA CONCEPTS UTILIZED (CSE 213 MAPPING)
    # =========================================================
    slide5 = prs.slides.add_slide(blank_layout)
    apply_bg(slide5)
    add_header(slide5, "Advanced Java Programming (CSE 213) Concepts", "04 • CURRICULUM IMPLEMENTATION")

    java_concepts = [
        ("JavaFX 21 GUI & Layouts", "BorderPane, VBox, HBox, TableView with SimpleStringProperty data binding, custom dialogs (CyberAlert), and CSS stylesheets (`gamer-theme.css`)."),
        ("Multithreading & Concurrency", "ExecutorService, Executors.newCachedThreadPool, Daemon threads (`nexus-cloud-bg-worker`), and Platform.runLater() for thread safety."),
        ("Thread-Safe Collections", "CopyOnWriteArrayList for lock-free read-heavy tournament queries and ConcurrentHashMap / ConcurrentSkipListSet for memory state."),
        ("Software Design Patterns", "Facade Pattern (ApiClient), Data Access Object Pattern (FirebaseClient), Data Transfer Object Pattern (DTOs), and Model-View-Controller (MVC)."),
        ("Enterprise gRPC & Cloud SDKs", "Google Cloud Firestore SDK, Firebase Admin APIs, token authentication, and Maven shaded fat JAR packaging."),
        ("Event-Driven Programming", "JavaFX ObservableList, Lambda expressions for event handlers, and functional consumers for asynchronous cloud update callbacks.")
    ]

    for idx, (title, desc) in enumerate(java_concepts):
        r = idx // 3
        c = idx % 3
        l = Inches(0.8 + c * 3.98)
        t = Inches(1.6 + r * 2.7)
        add_card(slide5, l, t, Inches(3.8), Inches(2.45), title=title, border_color=COLOR_RED if idx == 1 else COLOR_CARD_BORDER)
        
        tb = slide5.shapes.add_textbox(l + Inches(0.15), t + Inches(0.65), Inches(3.5), Inches(1.6))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = desc
        p.font.name = 'Arial'
        p.font.size = Pt(11.5)
        p.font.color.rgb = COLOR_GRAY

    # =========================================================
    # SLIDE 6: MULTI-GAME BRACKET & BR ENGINE
    # =========================================================
    slide6 = prs.slides.add_slide(blank_layout)
    apply_bg(slide6)
    add_header(slide6, "Bracket Algorithms & Multi-Game Support", "05 • CORE ALGORITHMIC LOGIC")

    add_card(slide6, Inches(0.8), Inches(1.6), Inches(5.75), Inches(5.3), title="ALGORITHMIC BRACKET GENERATION")
    tb_b = slide6.shapes.add_textbox(Inches(1.0), Inches(2.2), Inches(5.35), Inches(4.5))
    tf_b = tb_b.text_frame
    tf_b.word_wrap = True
    
    bracket_points = [
        "Power-of-Two Normalization: Automatically calculates closest 2^n bracket slots (4, 8, 16, 32 teams).",
        "Automatic BYE Seeding: Odd or non-power-of-two team counts receive automatic BYE passes to round 2.",
        "Recursive Winner Propagation: Finalizing a match dynamically advances the winning team ID to the next round slot.",
        "Battle Royale Matrix Engine: Computes cumulative match rankings, placement points, and kill bonuses (Erangel, Miramar, Kings Canyon)."
    ]
    for i, pt in enumerate(bracket_points):
        p = tf_b.paragraphs[0] if i == 0 else tf_b.add_paragraph()
        p.text = "•  " + pt
        p.font.name = 'Arial'
        p.font.size = Pt(12)
        p.font.color.rgb = COLOR_WHITE
        p.space_after = Pt(8)

    add_card(slide6, Inches(6.75), Inches(1.6), Inches(5.78), Inches(5.3), title="6 PRE-SEEDED ESPORTS TITLES")
    tb_g = slide6.shapes.add_textbox(Inches(6.95), Inches(2.2), Inches(5.35), Inches(4.5))
    tf_g = tb_g.text_frame
    tf_g.word_wrap = True

    games_list = [
        ("Counter-Strike 2 (CS2): ", "Cologne Major (NaVi, FaZe, Vitality, G2, Spirit)"),
        ("Valorant: ", "VCT Masters (Sentinels, Fnatic, PRX, DRX, Liquid)"),
        ("Dota 2: ", "The International (Spirit, Liquid, GG, Falcons, Secret)"),
        ("Apex Legends: ", "ALGS Championship (TSM, DarkZero, Alliance, OpTic)"),
        ("BGMI: ", "BGIS Championship (GodLike, Soul, XSpark, Blind)"),
        ("Rocket League: ", "RLCS World Championship (Karmine Corp, BDS, G2)")
    ]
    for i, (g, d) in enumerate(games_list):
        p = tf_g.paragraphs[0] if i == 0 else tf_g.add_paragraph()
        p.text = f"{i+1}. {g} {d}"
        p.font.name = 'Arial'
        p.font.size = Pt(11.5)
        p.font.color.rgb = COLOR_GRAY
        p.space_after = Pt(6)

    # =========================================================
    # SLIDE 7: SECURITY & INTEGRITY ARCHITECTURE
    # =========================================================
    slide7 = prs.slides.add_slide(blank_layout)
    apply_bg(slide7)
    add_header(slide7, "Security, Integrity & Audit Framework", "06 • SECURITY ARCHITECTURE")

    sec_cards = [
        ("Tournament-Level Master Security", 
         "Shifted from individual team passwords to a single Master Tournament Password (default: ADMIN). Organizers authenticate once per session, unlocking modifications across all tournament matches."),
        ("Fair-Play Result Disputes",
         "Participants can file official score disputes attaching spectator VOD and clip URLs. Organizers review disputes and apply binding decisions (Confirm Result, Reverse Winner, Replay Match)."),
        ("Conduct & Anti-Cheat Moderation",
         "Players suspected of third-party tools, ringing (impersonation), or toxicity are investigated under official tournament rules, allowing formal penalties (Warned, Suspended, Banned)."),
        ("Immutable Security Audit Trail",
         "Every modification, score validation, and moderation penalty is written to an append-only audit log with timestamp, operator ID, and action reason for academic accountability.")
    ]

    for idx, (h, d) in enumerate(sec_cards):
        r = idx // 2
        c = idx % 2
        l = Inches(0.8 + c * 5.95)
        t = Inches(1.6 + r * 2.7)
        add_card(slide7, l, t, Inches(5.75), Inches(2.45), title=h, border_color=COLOR_ORANGE if idx == 0 else COLOR_CARD_BORDER)
        
        tb = slide7.shapes.add_textbox(l + Inches(0.2), t + Inches(0.65), Inches(5.35), Inches(1.6))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = d
        p.font.name = 'Arial'
        p.font.size = Pt(12.5)
        p.font.color.rgb = COLOR_GRAY

    # =========================================================
    # SLIDE 8: UI WALKTHROUGH: DASHBOARD & TOURNAMENTS
    # =========================================================
    slide8 = prs.slides.add_slide(blank_layout)
    apply_bg(slide8)
    add_header(slide8, "Live UI Walkthrough: Operations & Tournaments", "07 • USER INTERFACE WALKTHROUGH")

    # Left: Dashboard Screenshot
    add_card(slide8, Inches(0.8), Inches(1.6), Inches(5.75), Inches(5.3), title="ORGANIZER COMMAND CENTER (DASHBOARD)")
    if os.path.exists("target/screenshots/screenshot_dashboard.png"):
        slide8.shapes.add_picture("target/screenshots/screenshot_dashboard.png", Inches(1.0), Inches(2.2), width=Inches(5.35))
    tb_s8_l = slide8.shapes.add_textbox(Inches(1.0), Inches(5.8), Inches(5.35), Inches(0.9))
    p = tb_s8_l.text_frame.paragraphs[0]
    p.text = "Displays real-time match counters, active tournaments, quick ops, and recent audit feed with zero UI latency."
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_GRAY

    # Right: Tournaments Screenshot
    add_card(slide8, Inches(6.75), Inches(1.6), Inches(5.78), Inches(5.3), title="TOURNAMENT LIFECYCLE MANAGEMENT")
    if os.path.exists("target/screenshots/screenshot_tournaments.png"):
        slide8.shapes.add_picture("target/screenshots/screenshot_tournaments.png", Inches(6.95), Inches(2.2), width=Inches(5.35))
    tb_s8_r = slide8.shapes.add_textbox(Inches(6.95), Inches(5.8), Inches(5.35), Inches(0.9))
    p = tb_s8_r.text_frame.paragraphs[0]
    p.text = "Configure rules, formats, capacity, and advance lifecycle states (Registration → Check-in → In Progress → Completed)."
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_GRAY

    # =========================================================
    # SLIDE 9: UI WALKTHROUGH: BRACKET & MATCH OPERATIONS
    # =========================================================
    slide9 = prs.slides.add_slide(blank_layout)
    apply_bg(slide9)
    add_header(slide9, "Live UI Walkthrough: Bracket Tree & Matches", "08 • USER INTERFACE WALKTHROUGH")

    # Left: Bracket Screenshot
    add_card(slide9, Inches(0.8), Inches(1.6), Inches(5.75), Inches(5.3), title="INTERACTIVE BRACKET TREE PROGRESSION")
    if os.path.exists("target/screenshots/screenshot_bracket.png"):
        slide9.shapes.add_picture("target/screenshots/screenshot_bracket.png", Inches(1.0), Inches(2.2), width=Inches(5.35))
    tb_s9_l = slide9.shapes.add_textbox(Inches(1.0), Inches(5.8), Inches(5.35), Inches(0.9))
    p = tb_s9_l.text_frame.paragraphs[0]
    p.text = "Click any match node to record live round scores and kills. Winners automatically advance across round columns."
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_GRAY

    # Right: Matches Screenshot
    add_card(slide9, Inches(6.75), Inches(1.6), Inches(5.78), Inches(5.3), title="LIVE MATCH OPERATIONS ROOM")
    if os.path.exists("target/screenshots/screenshot_matches.png"):
        slide9.shapes.add_picture("target/screenshots/screenshot_matches.png", Inches(6.95), Inches(2.2), width=Inches(5.35))
    tb_s9_r = slide9.shapes.add_textbox(Inches(6.95), Inches(5.8), Inches(5.35), Inches(0.9))
    p = tb_s9_r.text_frame.paragraphs[0]
    p.text = "Set ongoing matches to LIVE, edit frags, configure server room IDs & passwords, and confirm match winners."
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_GRAY

    # =========================================================
    # SLIDE 10: REAL-TIME CLOUD WEB PORTAL
    # =========================================================
    slide10 = prs.slides.add_slide(blank_layout)
    apply_bg(slide10)
    add_header(slide10, "Real-Time Cloud Web Portal (Firebase Hosting)", "09 • CLOUD INTEGRATION")

    add_card(slide10, Inches(0.8), Inches(1.6), Inches(6.2), Inches(5.3), title="LIVE CLOUD SYNCHRONIZATION")
    tb_p = slide10.shapes.add_textbox(Inches(1.0), Inches(2.2), Inches(5.8), Inches(4.5))
    tf_p = tb_p.text_frame
    tf_p.word_wrap = True

    web_points = [
        ("Public Web URL: ", "https://nexusarena-esports.web.app (100% serverless on Google Firebase Hosting)."),
        ("Bidirectional Data Flow: ", "When desktop organizers finalize a score, Firestore triggers onSnapshot listeners on web clients within milliseconds."),
        ("Instant First Render: ", "Pre-seeded prototype data guarantees zero blank screens or failed loading toasts on cold start."),
        ("Participant Controls: ", "Players register squads, view room IDs / passwords, file disputes, and download CSV standings directly."),
        ("In-App Auto-Updater: ", "Desktop client queries version.json for 1-click update checks without requiring manual reinstallations.")
    ]
    for i, (head, body) in enumerate(web_points):
        p = tf_p.paragraphs[0] if i == 0 else tf_p.add_paragraph()
        r1 = p.add_run()
        r1.text = "•  " + head
        r1.font.bold = True
        r1.font.color.rgb = COLOR_WHITE
        r2 = p.add_run()
        r2.text = body
        r2.font.color.rgb = COLOR_GRAY
        p.font.name = 'Arial'
        p.font.size = Pt(12)
        p.space_after = Pt(10)

    # Right: Disputes / Teams Screenshot
    add_card(slide10, Inches(7.2), Inches(1.6), Inches(5.33), Inches(5.3), title="TEAM ROSTERS & DISPUTES")
    if os.path.exists("target/screenshots/screenshot_teams.png"):
        slide10.shapes.add_picture("target/screenshots/screenshot_teams.png", Inches(7.35), Inches(2.2), width=Inches(5.0))
    tb_s10_r = slide10.shapes.add_textbox(Inches(7.35), Inches(5.8), Inches(5.0), Inches(0.9))
    p = tb_s10_r.text_frame.paragraphs[0]
    p.text = "Verified team rosters across CS2, Valorant, Dota 2, Apex, BGMI, and Rocket League with instant check-in toggles."
    p.font.size = Pt(11)
    p.font.color.rgb = COLOR_GRAY

    # =========================================================
    # SLIDE 11: TECHNICAL BENCHMARKS & RESULTS
    # =========================================================
    slide11 = prs.slides.add_slide(blank_layout)
    apply_bg(slide11)
    add_header(slide11, "Technical Benchmarks & Performance Results", "10 • RESULTS & EVALUATION")

    metrics = [
        ("0.001 ms", "UI Response Latency", "In-memory concurrent caching eliminates JavaFX thread freezes."),
        ("100% Cloud", "Serverless Deployment", "No local MySQL or Tomcat required; Google Cloud Firestore backend."),
        ("6 Titles", "Pre-Seeded Esports Games", "Full authentic rosters for CS2, Valorant, Dota 2, Apex, BGMI, RL."),
        ("1-Click Native", "Windows Executable", "Standalone NexusArena.exe with auto-JDK detection.")
    ]

    for idx, (val, title, desc) in enumerate(metrics):
        l = Inches(0.8 + idx * 2.98)
        add_card(slide11, l, Inches(1.6), Inches(2.78), Inches(2.6), border_color=COLOR_RED if idx == 0 else COLOR_CARD_BORDER)
        
        tb = slide11.shapes.add_textbox(l + Inches(0.15), Inches(1.8), Inches(2.48), Inches(2.2))
        tf = tb.text_frame
        p_val = tf.paragraphs[0]
        p_val.text = val
        p_val.font.name = 'Trebuchet MS'
        p_val.font.size = Pt(26)
        p_val.font.bold = True
        p_val.font.color.rgb = COLOR_ORANGE if idx != 0 else COLOR_RED
        
        p_tit = tf.add_paragraph()
        p_tit.text = title
        p_tit.font.name = 'Trebuchet MS'
        p_tit.font.size = Pt(13)
        p_tit.font.bold = True
        p_tit.font.color.rgb = COLOR_WHITE
        p_tit.space_before = Pt(4)

        p_desc = tf.add_paragraph()
        p_desc.text = desc
        p_desc.font.name = 'Arial'
        p_desc.font.size = Pt(11)
        p_desc.font.color.rgb = COLOR_GRAY
        p_desc.space_before = Pt(4)

    # Key Accomplishments Card
    add_card(slide11, Inches(0.8), Inches(4.5), Inches(11.73), Inches(2.4), title="KEY ARCHITECTURAL ACCOMPLISHMENTS")
    tb_acc = slide11.shapes.add_textbox(Inches(1.0), Inches(5.0), Inches(11.3), Inches(1.7))
    tf_acc = tb_acc.text_frame
    tf_acc.word_wrap = True

    accs = [
        "Eliminated UI thread locking: Offloaded all Firestore network operations to asynchronous daemon thread pools.",
        "Industrial side-panel navigation: Replaced congested top navigation with intuitive left drawer matching modern esports broadcast suites.",
        "Zero-configuration packaging: Delivered a 59MB Windows distribution bundle containing fat shaded JAR and native launcher."
    ]
    for i, a in enumerate(accs):
        p = tf_acc.paragraphs[0] if i == 0 else tf_acc.add_paragraph()
        p.text = "✓  " + a
        p.font.name = 'Arial'
        p.font.size = Pt(12)
        p.font.color.rgb = COLOR_WHITE
        p.space_after = Pt(6)

    # =========================================================
    # SLIDE 12: CONCLUSION, FUTURE SCOPE & Q&A
    # =========================================================
    slide12 = prs.slides.add_slide(blank_layout)
    apply_bg(slide12)
    add_header(slide12, "Conclusion, Future Enhancements & Q&A", "11 • CONCLUSION & ROADMAP")

    add_card(slide12, Inches(0.8), Inches(1.6), Inches(5.75), Inches(4.2), title="CONCLUSION & IMPACT")
    tb_con = slide12.shapes.add_textbox(Inches(1.0), Inches(2.2), Inches(5.35), Inches(3.4))
    tf_con = tb_con.text_frame
    tf_con.word_wrap = True

    con_points = [
        "Successfully solved tournament delays, score discrepancies, and venue congestion at SRM University – AP.",
        "Demonstrated mastery of Advanced Java (CSE 213): JavaFX 21, concurrency thread pools, thread-safe collections, design patterns, and cloud integration.",
        "Delivered a production-ready system actively deployed on Google Cloud and ready for university esports fests."
    ]
    for i, cp in enumerate(con_points):
        p = tf_con.paragraphs[0] if i == 0 else tf_con.add_paragraph()
        p.text = "•  " + cp
        p.font.name = 'Arial'
        p.font.size = Pt(12)
        p.font.color.rgb = COLOR_WHITE
        p.space_after = Pt(10)

    add_card(slide12, Inches(6.75), Inches(1.6), Inches(5.78), Inches(4.2), title="FUTURE ENHANCEMENTS")
    tb_fut = slide12.shapes.add_textbox(Inches(6.95), Inches(2.2), Inches(5.35), Inches(3.4))
    tf_fut = tb_fut.text_frame
    tf_fut.word_wrap = True

    fut_points = [
        "OBS Live Broadcast Overlays: Automated lower-third graphic overlays for live Twitch/YouTube tournament streaming.",
        "Game Server Webhooks: Direct API integration with CS2 and Valorant custom game loggers for automated round and frag counting.",
        "Mobile Captain Application: Android/iOS push notifications for match callouts and server room credential alerts."
    ]
    for i, fp in enumerate(fut_points):
        p = tf_fut.paragraphs[0] if i == 0 else tf_fut.add_paragraph()
        p.text = "•  " + fp
        p.font.name = 'Arial'
        p.font.size = Pt(12)
        p.font.color.rgb = COLOR_WHITE
        p.space_after = Pt(10)

    # Bottom Thank You / Q&A Banner
    qa_card = add_card(slide12, Inches(0.8), Inches(6.0), Inches(11.73), Inches(1.0), border_color=COLOR_RED)
    tb_qa = slide12.shapes.add_textbox(Inches(1.0), Inches(6.08), Inches(11.3), Inches(0.8))
    tf_qa = tb_qa.text_frame
    p_qa = tf_qa.paragraphs[0]
    p_qa.text = "THANK YOU! QUESTIONS & EVALUATION DISCUSSION"
    p_qa.font.name = 'Trebuchet MS'
    p_qa.font.size = Pt(18)
    p_qa.font.bold = True
    p_qa.font.color.rgb = COLOR_WHITE
    p_qa.alignment = PP_ALIGN.CENTER

    output_pptx = "NexusArena_Presentation.pptx"
    prs.save(output_pptx)
    print(f"Presentation generated successfully: {output_pptx}")

if __name__ == "__main__":
    build_presentation()
