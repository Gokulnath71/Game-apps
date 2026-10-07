import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

public class NeonNinja {

    static final int GW = 1000;
    static final int GH = 640;
    static final Preferences P = Preferences.userNodeForPackage(NeonNinja.class);

    static final Color CYAN = new Color(0, 240, 255);
    static final Color MAGENTA = new Color(255, 40, 220);
    static final Color LIME = new Color(150, 255, 40);
    static final Color GOLD = new Color(255, 205, 60);
    static final Color RED = new Color(255, 70, 90);
    static final Color ICE = new Color(215, 240, 255);
    static final Color DIM = new Color(120, 140, 170);
    static final Color BTN_BG = new Color(10, 16, 34);
    static final Color BTN_HI = new Color(0, 60, 90);

    static final String[] DIFF_NAMES = {"EASY", "NORMAL", "HARD", "NIGHTMARE"};
    static final double[] DIFF_MUL = {0.75, 1.0, 1.45, 1.95};

    static final String[] CH_NAMES = {"KAZE", "YUKI", "RONIN", "VIPER", "SPECTRE", "ONI"};
    static final Color[] CH_COL = {CYAN, ICE, GOLD, LIME, MAGENTA, RED};
    static final double[] CH_SPD = {5.2, 6.1, 4.4, 5.6, 5.9, 4.1};
    static final double[] CH_HP = {100, 80, 150, 90, 85, 170};
    static final double[] CH_DMG = {25, 20, 38, 30, 28, 44};
    static final int[] CH_COST = {0, 0, 0, 150, 250, 350};
    static final String[] CH_SKILL = {
            "Balanced blade master - all rounder",
            "Swift ice runner - fastest feet",
            "Heavy gold tank - huge health",
            "Venom striker - fast and sharp",
            "Ghost phase - elite dash cooldown",
            "Oni brute - massive damage"};

    static final String[] LV_NAMES = {"ROOFTOPS", "CYBER ALLEY", "DOJO ZERO", "NEON CORE", "VOID TEMPLE"};
    static final Color[] LV_TOP = {
            new Color(6, 10, 30), new Color(24, 4, 32), new Color(4, 20, 18),
            new Color(28, 4, 10), new Color(10, 4, 34)};
    static final Color[] LV_BOT = {
            new Color(0, 40, 70), new Color(70, 0, 60), new Color(0, 60, 45),
            new Color(80, 10, 20), new Color(40, 0, 90)};
    static final Color[] LV_ACC = {CYAN, MAGENTA, LIME, RED, new Color(170, 90, 255)};

    static final String[] MODES = {"CAMPAIGN", "QUICK FIGHT", "SURVIVAL", "TIME ATTACK", "BOSS RUSH", "TRAINING"};
    static final String[] UPG_NAMES = {"VITALITY CORE", "POWER CORE", "SPEED CORE", "DASH CORE"};
    static final String[] UPG_DESC = {
            "+15 Max HP per level", "+15% slash damage per level",
            "+6% move speed per level", "-12 dash cooldown per level"};
    static final int[] UPG_COST = {60, 120, 200, 300, 450};

    static JFrame frame;
    static CardLayout cards = new CardLayout();
    static JPanel root = new JPanel(cards);
    static Game game;
    static String screen = "menu";
    static final ArrayDeque<String> nav = new ArrayDeque<>();

    static String mode = "CAMPAIGN";
    static int level = 0;
    static int diff = 1;
    static int coins = 0;
    static int equipped = 0;
    static int unlockedLevels = 1;
    static boolean[] owned = new boolean[6];
    static int[] upg = new int[4];
    static boolean sound = true;
    static boolean shakeOn = true;
    static int particles = 1;
    static int selChar = 0;

    static JButton[] skinBtns = new JButton[6];
    static JButton[] rosterBtns = new JButton[6];
    static JButton[] upgBtns = new JButton[4];
    static JButton[] diffBtns = new JButton[4];
    static JButton[] lvBtns = new JButton[5];
    static JButton btnSound, btnShake, btnPart, btnDiff;
    static JLabel rosterInfo;
    static JTextArea scoreArea;
    static final List<JLabel> coinLabels = new ArrayList<>();

    static JLabel goHead, goLine1, goLine2, goLine3;
    static JLabel vicHead, vicLine1, vicLine2, vicLine3;
    static JLabel pauseStats;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NeonNinja::build);
    }

    static void build() {
        load();
        game = new Game();
        root.setPreferredSize(new Dimension(GW, GH));

        root.add(buildMenu(), "menu");
        root.add(buildModes(), "modes");
        root.add(buildLevels(), "levels");
        root.add(buildDifficulty(), "difficulty");
        root.add(buildRoster(), "roster");
        root.add(buildShop(), "shop");
        root.add(buildUpgrades(), "upgrades");
        root.add(buildSettings(), "settings");
        root.add(buildControls(), "controls");
        root.add(buildHelp(), "help");
        root.add(buildScores(), "scores");
        root.add(buildCredits(), "credits");
        root.add(game, "play");
        root.add(buildPause(), "pause");
        root.add(buildGameOver(), "gameover");
        root.add(buildVictory(), "victory");

        bindKeys();

        frame = new JFrame("NEON NINJA - Java Edition");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            public void windowIconified(WindowEvent e) {
                if ("play".equals(screen)) go("pause");
            }
        });
        cards.show(root, "menu");
        frame.setVisible(true);
        refresh();
    }

    static JPanel buildMenu() {
        JPanel p = new NeonPanel();
        p.setLayout(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(16, 26, 16, 26));
        p.setPreferredSize(new Dimension(GW, GH));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(new GlowTitle("NEON NINJA", CYAN, 62), BorderLayout.NORTH);
        JLabel sub = new JLabel("JAVA EDITION  -  NEON ACTION ARENA", SwingConstants.CENTER);
        sub.setFont(font(16, true));
        sub.setForeground(DIM);
        top.add(sub, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        String[] cmds = {
                "mode:CAMPAIGN", "mode:QUICK FIGHT",
                "mode:SURVIVAL", "mode:TIME ATTACK",
                "mode:BOSS RUSH", "mode:TRAINING",
                "nav:modes", "nav:levels",
                "nav:roster", "nav:shop",
                "nav:upgrades", "nav:difficulty",
                "nav:settings", "nav:controls",
                "nav:help", "nav:scores",
                "nav:credits", "exit"
        };
        String[] labels = {
                "STORY CAMPAIGN", "QUICK FIGHT",
                "SURVIVAL MODE", "TIME ATTACK",
                "BOSS RUSH", "TRAINING DOJO",
                "CUSTOM BATTLE", "LEVEL SELECT",
                "NINJA ROSTER", "NEON SHOP",
                "UPGRADES", "DIFFICULTY",
                "SETTINGS", "CONTROLS",
                "HOW TO PLAY", "HIGH SCORES",
                "CREDITS", "EXIT GAME"
        };
        JPanel grid = new JPanel(new GridLayout(9, 2, 12, 8));
        grid.setOpaque(false);
        for (int i = 0; i < labels.length; i++) grid.add(mkBtn(labels[i], cmds[i]));
        p.add(grid, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        JLabel cl = mkCoinLabel();
        south.add(cl, BorderLayout.WEST);
        JLabel ver = new JLabel("v2.0  |  build with Java + Swing", SwingConstants.CENTER);
        ver.setFont(font(13, false));
        ver.setForeground(DIM);
        south.add(ver, BorderLayout.CENTER);
        JLabel diffL = new JLabel("DIFFICULTY: " + DIFF_NAMES[diff], SwingConstants.RIGHT);
        diffL.setFont(font(14, true));
        diffL.setForeground(GOLD);
        south.add(diffL, BorderLayout.EAST);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    static JPanel buildModes() {
        String[][] items = {
                {"STORY CAMPAIGN  (10 waves, 2 bosses)", "mode:CAMPAIGN"},
                {"QUICK FIGHT  (5 waves)", "mode:QUICK FIGHT"},
                {"SURVIVAL  (endless waves)", "mode:SURVIVAL"},
                {"TIME ATTACK  (90 seconds)", "mode:TIME ATTACK"},
                {"BOSS RUSH  (5 bosses)", "mode:BOSS RUSH"},
                {"TRAINING DOJO  (no damage)", "mode:TRAINING"}};
        JComponent[] bs = new JComponent[items.length];
        for (int i = 0; i < items.length; i++) {
            JButton b = mkBtn(items[i][0], items[i][1]);
            b.setPreferredSize(new Dimension(540, 50));
            b.setMaximumSize(new Dimension(540, 50));
            bs[i] = b;
        }
        return screen("SELECT BATTLE MODE", column(bs));
    }

    static JPanel buildLevels() {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        for (int i = 0; i < 5; i++) {
            JButton b = mkBtn("", "lv:" + i);
            b.setMaximumSize(new Dimension(460, 48));
            b.setPreferredSize(new Dimension(460, 48));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            lvBtns[i] = b;
            center.add(b);
            center.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        center.add(Box.createVerticalGlue());
        return screen("SELECT ARENA", center);
    }

    static JPanel buildDifficulty() {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        String[] desc = {
                "Weak enemies, forgiving damage",
                "The intended neon experience",
                "Faster, meaner, tougher foes",
                "Only the fastest ninja survives"};
        for (int i = 0; i < 4; i++) {
            JButton b = mkBtn("", "diff:" + i);
            b.setMaximumSize(new Dimension(470, 52));
            b.setPreferredSize(new Dimension(470, 52));
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            diffBtns[i] = b;
            center.add(b);
            center.add(Box.createRigidArea(new Dimension(0, 6)));
            JLabel d = new JLabel(desc[i]);
            d.setFont(font(13, false));
            d.setForeground(DIM);
            d.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(d);
            center.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        center.add(Box.createVerticalGlue());
        JPanel start = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        start.setOpaque(false);
        start.add(mkBtn("<< BACK", "back"));
        start.add(mkBtn("START MISSION >>", "start"));
        return screen2("SELECT DIFFICULTY", center, start);
    }

    static JPanel buildRoster() {
        JPanel grid = new JPanel(new GridLayout(3, 2, 14, 12));
        grid.setOpaque(false);
        for (int i = 0; i < 6; i++) {
            JButton b = mkBtn("", "charsel:" + i);
            rosterBtns[i] = b;
            grid.add(b);
        }
        rosterInfo = new JLabel(" ", SwingConstants.CENTER);
        rosterInfo.setFont(font(16, true));
        rosterInfo.setForeground(GOLD);
        rosterInfo.setBorder(new EmptyBorder(10, 0, 0, 0));
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(mkCoinLabel(), BorderLayout.WEST);
        south.add(rosterInfo, BorderLayout.CENTER);
        south.add(mkBtn("BACK", "back"), BorderLayout.EAST);
        return screen2("NINJA ROSTER - SELECT FIGHTER", grid, south);
    }

    static JPanel buildShop() {
        JPanel grid = new JPanel(new GridLayout(3, 2, 14, 12));
        grid.setOpaque(false);
        for (int i = 0; i < 6; i++) {
            JButton b = mkBtn("", "skinbuy:" + i);
            skinBtns[i] = b;
            grid.add(b);
        }
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(mkCoinLabel(), BorderLayout.WEST);
        JLabel tip = new JLabel("Earn coins by fighting!", SwingConstants.CENTER);
        tip.setFont(font(14, false));
        tip.setForeground(DIM);
        south.add(tip, BorderLayout.CENTER);
        south.add(mkBtn("BACK", "back"), BorderLayout.EAST);
        return screen2("NEON SHOP - UNLOCK SKINS", grid, south);
    }

    static JPanel buildUpgrades() {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        for (int i = 0; i < 4; i++) {
            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(760, 58));
            JLabel info = new JLabel("<html><b>" + UPG_NAMES[i] + "</b><br><font color='#788caa' size='2'>"
                    + UPG_DESC[i] + "</font></html>");
            info.setFont(font(15, true));
            info.setForeground(CH_COL[i % CH_COL.length]);
            info.setPreferredSize(new Dimension(500, 54));
            JButton b = mkBtn("", "upg:" + i);
            b.setPreferredSize(new Dimension(230, 46));
            upgBtns[i] = b;
            row.add(info, BorderLayout.CENTER);
            row.add(b, BorderLayout.EAST);
            center.add(row);
            center.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        center.add(Box.createVerticalGlue());
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(mkCoinLabel(), BorderLayout.WEST);
        south.add(mkBtn("BACK", "back"), BorderLayout.EAST);
        return screen2("UPGRADE LAB - PERMANENT POWER", center, south);
    }

    static JPanel buildSettings() {
        JPanel p = screen("SETTINGS", null);
        JPanel center = column(
                mkBtn(" ", "toggle:diff"),
                mkBtn(" ", "toggle:sound"),
                mkBtn(" ", "toggle:shake"),
                mkBtn(" ", "toggle:particles"),
                mkBtn("RESET ALL PROGRESS", "reset"));
        btnDiff = (JButton) ((JPanel) center).getComponent(0);
        btnSound = (JButton) ((JPanel) center).getComponent(2);
        btnShake = (JButton) ((JPanel) center).getComponent(4);
        btnPart = (JButton) ((JPanel) center).getComponent(6);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    static JPanel buildControls() {
        JPanel p = new NeonPanel();
        p.setLayout(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(18, 26, 18, 26));
        p.setPreferredSize(new Dimension(GW, GH));
        JLabel t = new JLabel("CONTROLS", SwingConstants.CENTER);
        t.setFont(font(34, true));
        t.setForeground(CYAN);
        p.add(t, BorderLayout.NORTH);
        JTextArea ta = new JTextArea(
                "  MOVE LEFT / RIGHT .........  A  /  D   or   LEFT  /  RIGHT\n"
                        + "  JUMP  (double jump) .......  W  /  UP  /  SPACE\n"
                        + "  FAST FALL .................  S  /  DOWN\n"
                        + "  NEON SLASH ................  J  /  Z  /  LEFT MOUSE\n"
                        + "  PHASE DASH ................  K  /  X  /  SHIFT  /  RIGHT MOUSE\n"
                        + "  PAUSE  /  BACK ............  ESC  /  P\n"
                        + "\n"
                        + "  COMBO KILLS: chain eliminations within 2 seconds for score multipliers.\n"
                        + "  DASH grants invincibility - dash THROUGH enemies to escape.\n"
                        + "  POWER DROPS:  H heal   R rage   S shield   T time slow   $ coins\n"
                        + "  Bosses appear every 5th wave - watch their charge telegraph!\n"
                        + "\n"
                        + "  Save the game progress: coins, upgrades and scores are stored\n"
                        + "  automatically in your user profile.");
        ta.setFont(font(17, false));
        ta.setForeground(new Color(200, 225, 255));
        ta.setBackground(new Color(5, 8, 20));
        ta.setEditable(false);
        ta.setCaretColor(Color.WHITE);
        ta.setBorder(new EmptyBorder(14, 20, 14, 20));
        p.add(new JScrollPane(ta), BorderLayout.CENTER);
        p.add(mkBtn("BACK", "back"), BorderLayout.SOUTH);
        return p;
    }

    static JPanel buildHelp() {
        JPanel p = new NeonPanel();
        p.setLayout(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(18, 26, 18, 26));
        p.setPreferredSize(new Dimension(GW, GH));
        JLabel t = new JLabel("HOW TO PLAY", SwingConstants.CENTER);
        t.setFont(font(34, true));
        t.setForeground(CYAN);
        p.add(t, BorderLayout.NORTH);
        JTextArea ta = new JTextArea(
                "  1. Pick a MODE from the menu, choose an ARENA and a DIFFICULTY.\n"
                        + "  2. Select your NINJA in the ROSTER - each has unique stats.\n"
                        + "  3. Spend coins in the SHOP and UPGRADE LAB between battles.\n"
                        + "  4. Survive every wave. Clear all waves to win the mission.\n"
                        + "  5. Every 5th wave spawns a BOSS - dash to dodge charges.\n"
                        + "\n"
                        + "  SCORING:  grunt 25   dasher 40   flyer 35   boss 300\n"
                        + "  Combo multiplier raises your score up to x3.\n"
                        + "  Coins earned = coin drops + score / 25 + win bonus.\n"
                        + "\n"
                        + "  CHARACTERS:\n"
                        + "   KAZE     balanced blade master\n"
                        + "   YUKI     fastest runner, low health\n"
                        + "   RONIN    heavy tank with huge HP\n"
                        + "   VIPER    fast venomous striker\n"
                        + "   SPECTRE  elite dash cooldown\n"
                        + "   ONI      massive brute damage\n"
                        + "\n"
                        + "  Beat the campaign to unlock the next arena!");
        ta.setFont(font(16, false));
        ta.setForeground(new Color(200, 225, 255));
        ta.setBackground(new Color(5, 8, 20));
        ta.setEditable(false);
        ta.setBorder(new EmptyBorder(14, 20, 14, 20));
        p.add(new JScrollPane(ta), BorderLayout.CENTER);
        p.add(mkBtn("BACK", "back"), BorderLayout.SOUTH);
        return p;
    }

    static JPanel buildScores() {
        JPanel p = screen("HALL OF NEON - HIGH SCORES", null);
        scoreArea = new JTextArea();
        scoreArea.setFont(font(19, true));
        scoreArea.setForeground(CYAN);
        scoreArea.setBackground(new Color(5, 8, 20));
        scoreArea.setEditable(false);
        scoreArea.setBorder(new EmptyBorder(16, 30, 16, 30));
        p.add(new JScrollPane(scoreArea), BorderLayout.CENTER);
        return p;
    }

    static JPanel buildCredits() {
        JPanel p = screen("CREDITS", null);
        JPanel center = column(
                lbl("NEON NINJA - JAVA EDITION", GOLD, 30),
                lbl("A neon cyber-ninja action arena", DIM, 18),
                lbl(" ", DIM, 14),
                lbl("Built with Java + Swing", CYAN, 20),
                lbl("Rendering: Java2D neon glow engine", DIM, 16),
                lbl("Sound: synthesized sine-wave SFX", DIM, 16),
                lbl("Progress saved via java.util.prefs", DIM, 16),
                lbl(" ", DIM, 14),
                lbl("Thanks for playing!", MAGENTA, 22));
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    static JPanel buildPause() {
        JPanel p = new NeonPanel(true);
        p.setLayout(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(40, 26, 26, 26));
        p.setPreferredSize(new Dimension(GW, GH));
        JLabel t = new JLabel("PAUSED", SwingConstants.CENTER);
        t.setFont(font(56, true));
        t.setForeground(GOLD);
        p.add(t, BorderLayout.NORTH);
        pauseStats = new JLabel(" ", SwingConstants.CENTER);
        pauseStats.setFont(font(18, true));
        pauseStats.setForeground(DIM);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(column(
                mkBtn("RESUME FIGHT", "resume"),
                mkBtn("RESTART MISSION", "retry"),
                mkBtn("CONTROLS", "nav:controls"),
                mkBtn("CHANGE DIFFICULTY", "nav:difficulty"),
                mkBtn("MAIN MENU", "menu")), BorderLayout.CENTER);
        center.add(pauseStats, BorderLayout.SOUTH);
        p.add(center, BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        south.setOpaque(false);
        south.add(mkBtn("EXIT GAME", "exit"));
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    static JPanel buildGameOver() {
        JPanel p = new NeonPanel(true);
        p.setLayout(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(46, 26, 26, 26));
        p.setPreferredSize(new Dimension(GW, GH));
        goHead = new JLabel("YOU FELL", SwingConstants.CENTER);
        goHead.setFont(font(60, true));
        goHead.setForeground(RED);
        p.add(goHead, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        goLine1 = big(" ", GOLD);
        goLine2 = big(" ", CYAN);
        goLine3 = big(" ", DIM);
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        goLine1.setAlignmentX(Component.CENTER_ALIGNMENT);
        goLine2.setAlignmentX(Component.CENTER_ALIGNMENT);
        goLine3.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.add(goLine1);
        info.add(Box.createRigidArea(new Dimension(0, 8)));
        info.add(goLine2);
        info.add(Box.createRigidArea(new Dimension(0, 8)));
        info.add(goLine3);
        center.add(info, BorderLayout.NORTH);
        center.add(column(
                mkBtn("RETRY", "retry"),
                mkBtn("NINJA ROSTER", "nav:roster"),
                mkBtn("NEON SHOP", "nav:shop"),
                mkBtn("UPGRADES", "nav:upgrades"),
                mkBtn("MAIN MENU", "menu")), BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    static JPanel buildVictory() {
        JPanel p = new NeonPanel(true);
        p.setLayout(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(46, 26, 26, 26));
        p.setPreferredSize(new Dimension(GW, GH));
        vicHead = new JLabel("MISSION CLEAR", SwingConstants.CENTER);
        vicHead.setFont(font(56, true));
        vicHead.setForeground(LIME);
        p.add(vicHead, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        vicLine1 = big(" ", GOLD);
        vicLine2 = big(" ", CYAN);
        vicLine3 = big(" ", DIM);
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        vicLine1.setAlignmentX(Component.CENTER_ALIGNMENT);
        vicLine2.setAlignmentX(Component.CENTER_ALIGNMENT);
        vicLine3.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.add(vicLine1);
        info.add(Box.createRigidArea(new Dimension(0, 8)));
        info.add(vicLine2);
        info.add(Box.createRigidArea(new Dimension(0, 8)));
        info.add(vicLine3);
        center.add(info, BorderLayout.NORTH);
        center.add(column(
                mkBtn("NEXT ARENA", "next"),
                mkBtn("PLAY AGAIN", "retry"),
                mkBtn("UPGRADES", "nav:upgrades"),
                mkBtn("MAIN MENU", "menu")), BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    static void handle(String cmd) {
        String[] parts = cmd.split(":", 2);
        String k = parts[0];
        switch (k) {
            case "nav":
                nav(parts[1]);
                break;
            case "back":
                back();
                break;
            case "menu":
                nav.clear();
                go("menu");
                break;
            case "mode":
                mode = parts[1];
                nav("levels");
                break;
            case "lv": {
                int i = Integer.parseInt(parts[1]);
                if (!levelUnlocked(i)) {
                    Sound.beep(160, 90, 0.25);
                    return;
                }
                level = i;
                nav("difficulty");
                break;
            }
            case "diff":
                diff = Integer.parseInt(parts[1]);
                Sound.beep(700, 50, 0.2);
                save();
                refresh();
                break;
            case "start":
                startGame();
                break;
            case "retry":
                startGame();
                break;
            case "next":
                if (level + 1 < 5 && levelUnlocked(level + 1)) level++;
                startGame();
                break;
            case "resume":
                go("play");
                game.tm.start();
                break;
            case "charsel": {
                selChar = Integer.parseInt(parts[1]);
                if (owned[selChar]) {
                    equipped = selChar;
                    Sound.beep(880, 60, 0.2);
                    save();
                } else Sound.beep(180, 70, 0.25);
                refresh();
                break;
            }
            case "skinbuy": {
                int i = Integer.parseInt(parts[1]);
                if (owned[i]) {
                    equipped = i;
                    save();
                } else if (coins >= CH_COST[i]) {
                    coins -= CH_COST[i];
                    owned[i] = true;
                    equipped = i;
                    Sound.beep(980, 90, 0.25);
                    save();
                } else {
                    Sound.beep(150, 100, 0.25);
                }
                refresh();
                break;
            }
            case "upg": {
                int i = Integer.parseInt(parts[1]);
                if (upg[i] >= 5) {
                    Sound.beep(150, 80, 0.25);
                } else if (coins >= UPG_COST[upg[i]]) {
                    coins -= UPG_COST[upg[i]];
                    upg[i]++;
                    Sound.beep(1050, 90, 0.25);
                    save();
                } else Sound.beep(150, 100, 0.25);
                refresh();
                break;
            }
            case "toggle":
                toggle(parts[1]);
                break;
            case "reset":
                if (JOptionPane.showConfirmDialog(frame, "Erase coins, upgrades, skins and scores?",
                        "RESET PROGRESS", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        P.clear();
                    } catch (Exception ex) {
                    }
                    load();
                    refresh();
                }
                break;
            case "exit":
                if (JOptionPane.showConfirmDialog(frame, "Quit Neon Ninja?", "EXIT",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
                break;
            default:
                break;
        }
    }

    static void toggle(String what) {
        switch (what) {
            case "diff":
                diff = (diff + 1) % 4;
                break;
            case "sound":
                sound = !sound;
                break;
            case "shake":
                shakeOn = !shakeOn;
                break;
            case "particles":
                particles = particles == 1 ? 0 : 1;
                break;
            default:
                break;
        }
        Sound.beep(760, 50, 0.2);
        save();
        refresh();
    }

    static void refresh() {
        String c = coins + " COINS";
        for (JLabel l : coinLabels) l.setText(c);

        for (int i = 0; i < 6; i++) {
            if (skinBtns[i] == null) continue;
            if (owned[i]) {
                skinBtns[i].setText(CH_NAMES[i] + (equipped == i ? "  [EQUIPPED]" : "  [OWNED]"));
                skinBtns[i].setForeground(LIME);
            } else {
                skinBtns[i].setText(CH_NAMES[i] + "  -  " + CH_COST[i] + " COINS");
                skinBtns[i].setForeground(CH_COL[i]);
            }
            if (rosterBtns[i] == null) continue;
            String st = owned[i] ? (equipped == i ? "  [ACTIVE]" : "  [SELECT]") : "  [LOCKED " + CH_COST[i] + "]";
            rosterBtns[i].setText(CH_NAMES[i] + st);
            rosterBtns[i].setForeground(owned[i] ? (equipped == i ? GOLD : CH_COL[i]) : DIM);
        }
        if (rosterInfo != null) {
            rosterInfo.setText(CH_NAMES[selChar] + " : " + CH_SKILL[selChar]
                    + "  |  HP " + (int) CH_HP[selChar] + "  SPD " + CH_SPD[selChar] + "  DMG " + (int) CH_DMG[selChar]);
        }
        for (int i = 0; i < 4; i++) {
            if (upgBtns[i] == null) continue;
            if (upg[i] >= 5) {
                upgBtns[i].setText("MAX LEVEL");
                upgBtns[i].setForeground(GOLD);
            } else {
                upgBtns[i].setText("LV " + upg[i] + "/5  -  " + UPG_COST[upg[i]] + " C");
                upgBtns[i].setForeground(coins >= UPG_COST[upg[i]] ? LIME : DIM);
            }
        }
        for (int i = 0; i < 4; i++) {
            if (diffBtns[i] == null) continue;
            diffBtns[i].setText((diff == i ? ">> " : "    ") + DIFF_NAMES[i]);
            diffBtns[i].setForeground(diff == i ? GOLD : DIM);
        }
        for (int i = 0; i < 5; i++) {
            if (lvBtns[i] == null) continue;
            if (levelUnlocked(i)) {
                lvBtns[i].setText((level == i ? ">> " : "    ") + "ARENA " + (i + 1) + " - " + LV_NAMES[i]);
                lvBtns[i].setForeground(level == i ? GOLD : LV_ACC[i]);
                lvBtns[i].setEnabled(true);
            } else {
                lvBtns[i].setText("    LOCKED - ARENA " + (i + 1));
                lvBtns[i].setForeground(new Color(70, 80, 100));
                lvBtns[i].setEnabled(false);
            }
        }
        if (btnDiff != null) btnDiff.setText("DIFFICULTY: " + DIFF_NAMES[diff]);
        if (btnSound != null) btnSound.setText("SOUND EFFECTS: " + (sound ? "ON" : "OFF"));
        if (btnShake != null) btnShake.setText("SCREEN SHAKE: " + (shakeOn ? "ON" : "OFF"));
        if (btnPart != null) btnPart.setText("PARTICLE EFFECTS: " + (particles == 1 ? "HIGH" : "LOW"));
        if (scoreArea != null) scoreArea.setText(scoreText());
        if (game != null && screen.equals("gameover")) {
            goLine1.setText("SCORE  " + game.score + "     KILLS  " + game.kills);
            goLine2.setText("WAVE " + game.wave + "  -  " + mode + "  -  " + DIFF_NAMES[diff]);
            goLine3.setText("COINS EARNED  " + game.gained + "   |   BANK  " + coins);
        }
        if (game != null && screen.equals("victory")) {
            vicLine1.setText("SCORE  " + game.score + "     KILLS  " + game.kills);
            vicLine2.setText(mode + "  -  " + LV_NAMES[level] + "  -  " + DIFF_NAMES[diff]);
            vicLine3.setText("COINS EARNED  " + game.gained + "   |   BANK  " + coins);
        }
        if (game != null && screen.equals("pause")) {
            pauseStats.setText(mode + "  |  " + LV_NAMES[level] + "  |  WAVE " + game.wave
                    + "  |  SCORE " + game.score);
        }
    }

    static String scoreText() {
        String raw = P.get("scores", "");
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %-4s %-12s %-16s %-10s %-10s%n", "#", "NINJA", "MODE", "SCORE", "WAVE"));
        sb.append("  --------------------------------------------------------\n");
        if (raw.isEmpty()) {
            sb.append("\n  No records yet. Go fight!");
        } else {
            String[] rows = raw.split(";");
            int n = 1;
            for (String r : rows) {
                if (r.isEmpty()) continue;
                String[] a = r.split("\\|", -1);
                if (a.length < 4) continue;
                sb.append(String.format("  %-4d %-12s %-16s %-10s %-10s%n", n++, a[0], a[1], a[2], a[3]));
            }
        }
        return sb.toString();
    }

    static boolean levelUnlocked(int i) {
        return i < unlockedLevels;
    }

    static void nav(String s) {
        nav.push(screen);
        go(s);
    }

    static void back() {
        String prev = nav.isEmpty() ? "menu" : nav.pop();
        if (prev.equals(screen)) prev = "menu";
        go(prev);
    }

    static void go(String s) {
        if (!s.equals("play") && game != null) {
            game.tm.stop();
            game.held.clear();
        }
        screen = s;
        refresh();
        cards.show(root, s);
        root.requestFocusInWindow();
    }

    static void startGame() {
        nav.clear();
        go("play");
        game.startRun();
        game.tm.start();
    }

    static void save() {
        P.put("coins", String.valueOf(coins));
        P.putInt("equipped", equipped);
        P.putInt("diff", diff);
        P.putInt("level", level);
        P.putBoolean("sound", sound);
        P.putBoolean("shake", shakeOn);
        P.putInt("particles", particles);
        P.putInt("unlocked", unlockedLevels);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) sb.append(owned[i] ? "1" : "0");
        P.put("owned", sb.toString());
        for (int i = 0; i < 4; i++) P.putInt("upg" + i, upg[i]);
    }

    static void load() {
        coins = P.getInt("coins", 0);
        equipped = Math.min(5, Math.max(0, P.getInt("equipped", 0)));
        diff = Math.min(3, Math.max(0, P.getInt("diff", 1)));
        level = Math.min(4, Math.max(0, P.getInt("level", 0)));
        sound = P.getBoolean("sound", true);
        shakeOn = P.getBoolean("shake", true);
        particles = P.getInt("particles", 1);
        unlockedLevels = Math.min(5, Math.max(1, P.getInt("unlocked", 1)));
        String ow = P.get("owned", "111100");
        for (int i = 0; i < 6; i++) owned[i] = i < ow.length() && ow.charAt(i) == '1';
        owned[0] = true;
        for (int i = 0; i < 4; i++) upg[i] = Math.min(5, Math.max(0, P.getInt("upg" + i, 0)));
        selChar = equipped;
    }

    static void saveScore(int sc, String md, int wv) {
        String n = JOptionPane.showInputDialog(frame,
                "HIGH SCORE " + sc + "\nEnter your ninja name:", "SCORE SAVED", JOptionPane.PLAIN_MESSAGE);
        if (n == null) n = "NINJA";
        n = n.trim().toUpperCase();
        if (n.isEmpty()) n = "NINJA";
        if (n.length() > 10) n = n.substring(0, 10);
        List<String[]> rows = new ArrayList<>();
        String raw = P.get("scores", "");
        if (!raw.isEmpty()) {
            for (String r : raw.split(";")) {
                String[] a = r.split("\\|", -1);
                if (a.length >= 4) rows.add(a);
            }
        }
        rows.add(new String[]{n, md, String.valueOf(sc), String.valueOf(wv)});
        rows.sort((a, b) -> Integer.compare(Integer.parseInt(b[2]), Integer.parseInt(a[2])));
        while (rows.size() > 10) rows.remove(rows.size() - 1);
        StringBuilder sb = new StringBuilder();
        for (String[] r : rows) sb.append(String.join("|", r)).append(";");
        P.put("scores", sb.toString());
    }

    static void bindKeys() {
        bind("LEFT", "left");
        bind("A", "left");
        bind("RIGHT", "right");
        bind("D", "right");
        bind("UP", "jump");
        bind("W", "jump");
        bind("SPACE", "jump");
        bind("DOWN", "down");
        bind("S", "down");
        bind("J", "attack");
        bind("Z", "attack");
        bind("K", "dash");
        bind("X", "dash");
        bind("SHIFT", "dash");
        bind("ESCAPE", "esc");
        bind("P", "esc");
    }

    static void bind(String key, String act) {
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke("pressed " + key), "p:" + act);
        im.put(KeyStroke.getKeyStroke("released " + key), "r:" + act);
        am.put("p:" + act, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                game.press(act);
            }
        });
        am.put("r:" + act, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                game.release(act);
            }
        });
    }

    static Font font(int size, boolean bold) {
        return new Font("Consolas", bold ? Font.BOLD : Font.PLAIN, size);
    }

    static JLabel lbl(String text, Color c, int size) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font(size, true));
        l.setForeground(c);
        return l;
    }

    static JLabel big(String text, Color c) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font(24, true));
        l.setForeground(c);
        return l;
    }

    static JLabel mkCoinLabel() {
        JLabel l = new JLabel(coins + " COINS");
        l.setFont(font(17, true));
        l.setForeground(GOLD);
        coinLabels.add(l);
        return l;
    }

    static JButton mkBtn(String text, String cmd) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(isEnabled() ? new Color(0, 220, 255, 150) : new Color(90, 100, 120, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(font(17, true));
        b.setForeground(CYAN);
        b.setBackground(BTN_BG);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(340, 42));
        b.addActionListener(e -> {
            Sound.beep(640, 45, 0.18);
            handle(cmd);
        });
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled()) {
                    b.setForeground(Color.WHITE);
                    b.setBackground(BTN_HI);
                }
            }

            public void mouseExited(MouseEvent e) {
                b.setForeground(CYAN);
                b.setBackground(BTN_BG);
            }
        });
        return b;
    }

    static JPanel column(JComponent... comps) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (JComponent c : comps) {
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(c);
            p.add(Box.createRigidArea(new Dimension(0, 9)));
        }
        return p;
    }

    static JPanel screen(String title, JComponent center) {
        return screen2(title, center, null);
    }

    static JPanel screen2(String title, JComponent center, JComponent customSouth) {
        JPanel p = new NeonPanel();
        p.setLayout(new BorderLayout(12, 12));
        p.setBorder(new EmptyBorder(18, 26, 18, 26));
        p.setPreferredSize(new Dimension(GW, GH));
        if (title != null) {
            JLabel t = new JLabel(title, SwingConstants.CENTER);
            t.setFont(font(32, true));
            t.setForeground(CYAN);
            p.add(t, BorderLayout.NORTH);
        }
        if (center != null) p.add(center, BorderLayout.CENTER);
        if (customSouth != null) {
            p.add(customSouth, BorderLayout.SOUTH);
        } else {
            JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
            south.setOpaque(false);
            south.add(mkBtn("< BACK", "back"));
            p.add(south, BorderLayout.SOUTH);
        }
        return p;
    }

    static class NeonPanel extends JPanel {
        boolean dark;

        NeonPanel() {
            this(false);
        }

        NeonPanel(boolean d) {
            dark = d;
            setOpaque(true);
        }

        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color a = dark ? new Color(4, 4, 10) : new Color(6, 9, 22);
            Color b = dark ? new Color(16, 4, 24) : new Color(10, 22, 44);
            g.setPaint(new GradientPaint(0, 0, a, 0, getHeight(), b));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(0, 220, 255, 16));
            for (int x = 0; x < getWidth(); x += 40) g.drawLine(x, 0, x, getHeight());
            for (int y = 0; y < getHeight(); y += 40) g.drawLine(0, y, getWidth(), y);
            g.setColor(new Color(0, 220, 255, 45));
            g.setStroke(new BasicStroke(2f));
            g.drawRect(6, 6, getWidth() - 12, getHeight() - 12);
        }
    }

    static class GlowTitle extends JComponent {
        final String text;
        final Color c;
        final int size;

        GlowTitle(String t, Color col, int s) {
            text = t;
            c = col;
            size = s;
            setPreferredSize(new Dimension(GW, size + 30));
        }

        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(font(size, true));
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = fm.getAscent() + 6;
            for (int i = 8; i >= 1; i--) {
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 10 * i));
                g.drawString(text, x, y);
            }
            g.setColor(c);
            g.drawString(text, x, y);
        }
    }

    static class Sound {
        static void beep(double freq, int ms, double vol) {
            if (!sound) return;
            new Thread(() -> {
                try {
                    int rate = 22050;
                    int n = Math.max(1, rate * ms / 1000);
                    byte[] buf = new byte[n * 2];
                    for (int i = 0; i < n; i++) {
                        double t = (double) i / rate;
                        double env = 1.0 - (double) i / n;
                        short v = (short) (Math.sin(2 * Math.PI * freq * t) * 32767 * vol * env);
                        buf[i * 2] = (byte) (v & 0xff);
                        buf[i * 2 + 1] = (byte) ((v >> 8) & 0xff);
                    }
                    AudioFormat fmt = new AudioFormat(rate, 16, 1, true, false);
                    SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
                    line.open(fmt);
                    line.start();
                    line.write(buf, 0, buf.length);
                    line.drain();
                    line.close();
                } catch (Exception ignored) {
                }
            }, "sfx").start();
        }
    }

    static void neon(Graphics2D g, Shape s, Color c, float w) {
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        Color glow = new Color(c.getRed(), c.getGreen(), c.getBlue(), 45);
        Color mid = new Color(c.getRed(), c.getGreen(), c.getBlue(), 120);
        g.setColor(glow);
        g.setStroke(new BasicStroke(w * 5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(s);
        g.setColor(mid);
        g.setStroke(new BasicStroke(w * 2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(s);
        g.setColor(c);
        g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(s);
    }

    static class Enemy {
        double x, y, w, h, vx, vy, hp, maxHp, speed, dmg, timer, atk, flash;
        int type, state, dir = 1;
        Color c;
    }

    static class Part {
        double x, y, vx, vy, life, maxLife, size;
        Color c;
        int kind;
    }

    static class Power {
        double x, y, vy, t;
        int type;
    }

    static class Plat {
        double x, y, w, h;
    }

    static class Game extends JPanel implements ActionListener, MouseListener {
        javax.swing.Timer tm = new javax.swing.Timer(16, this);
        Random R = new Random();

        double px = 120, py, vx, vy, pw = 30, ph = 46;
        int face = 1, jumps, jumpBuf, groundFlag;
        double maxHp, hp, speed, slashDmg, dashCd, dashCdMax = 100;
        double atkT, invuln, swingId, rageT, slowT, t;
        int shield, gained;
        boolean kLeft, kRight;
        final Set<String> held = new HashSet<>();

        List<Enemy> es = new ArrayList<>();
        List<Part> ps = new ArrayList<>();
        List<Power> ups = new ArrayList<>();
        List<Plat> plats = new ArrayList<>();
        List<int[]> queue = new ArrayList<>();
        List<double[]> trail = new ArrayList<>();
        int[][] skyline = new int[34][3];

        int wave, totalWaves, waveDelay, waveBanner, kills, combo, comboT, score, runCoins, shake, spawnT;
        double timeLeft;
        boolean ended, saved;
        boolean isTime, isTraining;

        Game() {
            setPreferredSize(new Dimension(GW, GH));
            setFocusable(true);
            addMouseListener(this);
        }

        int groundY() {
            return GH - 70;
        }

        void startRun() {
            maxHp = CH_HP[equipped] + 15 * upg[0];
            hp = maxHp;
            speed = CH_SPD[equipped] * (1 + 0.06 * upg[2]);
            if (equipped == 4) speed *= 1.05;
            slashDmg = CH_DMG[equipped] * (1 + 0.15 * upg[1]);
            dashCdMax = 100 - 12 * upg[3];
            if (equipped == 4) dashCdMax -= 18;
            dashCd = 0;
            px = 120;
            py = groundY() - ph;
            vx = vy = 0;
            face = 1;
            atkT = invuln = rageT = slowT = 0;
            shield = 0;
            swingId = 0;
            es.clear();
            ps.clear();
            ups.clear();
            queue.clear();
            trail.clear();
            plats.clear();
            wave = 0;
            kills = 0;
            combo = 0;
            comboT = 0;
            score = 0;
            runCoins = 0;
            gained = 0;
            shake = 0;
            spawnT = 0;
            ended = false;
            saved = false;
            t = 0;
            isTime = mode.equals("TIME ATTACK");
            isTraining = mode.equals("TRAINING");
            timeLeft = 90;
            if (mode.equals("CAMPAIGN")) totalWaves = 10;
            else if (mode.equals("QUICK FIGHT")) totalWaves = 5;
            else if (mode.equals("BOSS RUSH")) totalWaves = 5;
            else totalWaves = 0;
            buildLevel();
            waveDelay = 70;
            waveBanner = 70;
            waveBanner = 0;
        }

        void buildLevel() {
            int[][] base = {{130, 466, 230, 16}, {640, 466, 230, 16}, {385, 350, 230, 16}};
            for (int i = 0; i < base.length; i++) {
                Plat p = new Plat();
                p.x = base[i][0] + (level * 8);
                p.y = base[i][1] - level * 6;
                p.w = base[i][2];
                p.h = base[i][3];
                plats.add(p);
            }
            Random r = new Random(level * 77 + 3);
            for (int i = 0; i < skyline.length; i++) {
                skyline[i][0] = i * 32 + r.nextInt(10);
                skyline[i][1] = 60 + r.nextInt(170);
                skyline[i][2] = r.nextInt(4);
            }
        }

        void press(String k) {
            if (k.equals("esc")) {
                if ("play".equals(screen)) go("pause");
                else if ("pause".equals(screen)) {
                    go("play");
                    tm.start();
                } else if (!"menu".equals(screen)) back();
                return;
            }
            if (!"play".equals(screen) || ended) return;
            if (held.contains(k)) return;
            held.add(k);
            if (k.equals("jump")) jumpBuf = 6;
            if (k.equals("attack")) tryAttack();
            if (k.equals("dash")) tryDash();
        }

        void release(String k) {
            held.remove(k);
        }

        void tryAttack() {
            if (atkT > 0 || ended) return;
            atkT = 12;
            swingId++;
            Sound.beep(face > 0 ? 520 : 470, 45, 0.16);
        }

        void tryDash() {
            if (dashCd > 0 || ended) return;
            dashCd = dashCdMax;
            vx = face * 17;
            vy = Math.min(vy, 0);
            invuln = Math.max(invuln, 20);
            shake = Math.max(shake, 4);
            for (int i = 0; i < 10 * Math.max(1, particles); i++) {
                Part p = new Part();
                p.x = px + pw / 2;
                p.y = py + ph / 2;
                p.vx = -face * (1 + R.nextDouble() * 4) + (R.nextDouble() - 0.5) * 2;
                p.vy = (R.nextDouble() - 0.5) * 4;
                p.life = p.maxLife = 22;
                p.size = 3 + R.nextDouble() * 3;
                p.c = CH_COL[equipped];
                ps.add(p);
            }
            Sound.beep(300, 70, 0.2);
        }

        void step() {
            if (ended) return;
            t++;
            kLeft = held.contains("left");
            kRight = held.contains("right");

            if (atkT > 0) atkT--;
            if (invuln > 0) invuln--;
            if (dashCd > 0) dashCd--;
            if (rageT > 0) rageT--;
            if (slowT > 0) slowT--;
            if (comboT > 0) {
                comboT--;
                if (comboT == 0) combo = 0;
            }
            if (shake > 0) shake--;
            if (waveBanner > 0) waveBanner--;

            double move = speed;
            boolean dashing = dashCd > dashCdMax - 12;
            if (dashing) {
                vx = face * 16;
            } else if (kLeft && !kRight) {
                vx = -move;
                face = -1;
            } else if (kRight && !kLeft) {
                vx = move;
                face = 1;
            } else {
                vx *= 0.82;
                if (Math.abs(vx) < 0.3) vx = 0;
            }

            vy += 0.85;
            if (vy > 18) vy = 18;

            if (jumpBuf > 0) {
                jumpBuf--;
                if (groundFlag > 0) {
                    vy = -14.5;
                    jumps = 1;
                    groundFlag = 0;
                    Sound.beep(760, 50, 0.15);
                    puff(px + pw / 2, py + ph, CH_COL[equipped], 8);
                } else if (jumps < 2) {
                    vy = -13;
                    jumps++;
                    Sound.beep(880, 50, 0.15);
                    puff(px + pw / 2, py + ph / 2, CH_COL[equipped], 10);
                }
            }

            double prevBottom = py + ph;
            px += vx;
            py += vy;
            if (px < 8) px = 8;
            if (px + pw > GW - 8) px = GW - 8 - pw;

            groundFlag = 0;
            if (py + ph >= groundY()) {
                py = groundY() - ph;
                vy = 0;
                groundFlag = 1;
                jumps = 0;
            }
            if (vy >= 0) {
                for (Plat pl : plats) {
                    if (prevBottom <= pl.y && py + ph >= pl.y && px + pw > pl.x && px < pl.x + pl.w) {
                        py = pl.y - ph;
                        vy = 0;
                        groundFlag = 1;
                        jumps = 0;
                    }
                }
            }

            if (Math.abs(vx) > 1 && groundFlag > 0 && R.nextInt(3) == 0 && particles == 1) {
                Part p = new Part();
                p.x = px + pw / 2 - face * 8;
                p.y = py + ph - 2;
                p.vx = -face * (0.5 + R.nextDouble());
                p.vy = -0.5 - R.nextDouble();
                p.life = p.maxLife = 16;
                p.size = 2 + R.nextDouble() * 2;
                p.c = new Color(120, 160, 200);
                ps.add(p);
            }
            trail.add(new double[]{px, py, 1});
            if (trail.size() > 8) trail.remove(0);

            if (atkT >= 4 && atkT <= 11) {
                double hx = face > 0 ? px + pw - 8 : px - 62;
                Rectangle2D hit = new Rectangle2D.Double(hx, py - 8, 70, ph + 16);
                for (Enemy e : es) {
                    if (e.hp <= 0) continue;
                    if (hit.intersects(e.x, e.y, e.w, e.h)) {
                        hurtEnemy(e, slashDmg * (rageT > 0 ? 2 : 1), face);
                    }
                }
            }

            updateWaveLogic();

            double slow = slowT > 0 ? 0.45 : 1.0;
            for (Enemy e : new ArrayList<>(es)) {
                updateEnemy(e, slow);
                if (e.hp <= 0) killEnemy(e);
            }

            for (Iterator<Power> it = ups.iterator(); it.hasNext(); ) {
                Power u = it.next();
                u.t++;
                u.y += u.vy;
                u.vy += 0.05;
                double gx = px + pw / 2 - u.x;
                double gy = py + ph / 2 - u.y;
                double d = Math.hypot(gx, gy);
                if (d < 90) {
                    u.x += gx * 0.08;
                    u.y += gy * 0.08;
                }
                if (u.y > groundY() - 20) {
                    u.y = groundY() - 20;
                    u.vy = 0;
                }
                if (d < 26) {
                    applyPower(u.type);
                    it.remove();
                } else if (u.t > 900) it.remove();
            }

            for (Iterator<Part> it = ps.iterator(); it.hasNext(); ) {
                Part p = it.next();
                p.x += p.vx;
                p.y += p.vy;
                p.vy += 0.12;
                p.vx *= 0.97;
                p.life--;
                if (p.life <= 0) it.remove();
            }
            if (ps.size() > 400) ps.subList(0, ps.size() - 400).clear();

            if (isTime) {
                timeLeft -= 1.0 / 60.0;
                spawnT--;
                if (spawnT <= 0) {
                    spawnT = Math.max(24, 70 - (int) ((90 - timeLeft) * 0.5));
                    int r = R.nextInt(10);
                    spawnEnemy(r < 5 ? 0 : r < 8 ? 1 : 2);
                }
                if (timeLeft <= 0) {
                    timeLeft = 0;
                    finish(true);
                }
            }
        }

        void updateWaveLogic() {
            if (isTime) return;
            if (isTraining) {
                if (waveDelay > 0) {
                    waveDelay--;
                    if (waveDelay == 0) startWave();
                    return;
                }
                int dummies = 0;
                for (Enemy e : es) if (e.type == 4) dummies++;
                if (dummies + queue.size() < 6) queue.add(new int[]{4, 25});
                return;
            }
            if (waveDelay > 0) {
                waveDelay--;
                if (waveDelay == 0) startWave();
                return;
            }
            if (queue.isEmpty() && es.isEmpty()) {
                if (totalWaves > 0 && wave >= totalWaves) {
                    finish(true);
                } else {
                    waveDelay = 95;
                    waveBanner = 0;
                }
            }
        }

        void startWave() {
            wave++;
            queue.clear();
            waveBanner = 80;
            if (isTraining) {
                for (int i = 0; i < 6; i++) queue.add(new int[]{4, i * 14});
                return;
            }
            if (mode.equals("BOSS RUSH")) {
                queue.add(new int[]{3, 40});
                return;
            }
            boolean bossWave = wave % 5 == 0;
            if (bossWave) {
                queue.add(new int[]{3, 45});
                queue.add(new int[]{0, 110});
                queue.add(new int[]{1, 150});
                queue.add(new int[]{2, 180});
                return;
            }
            int g = 3 + wave + level;
            int d = wave >= 2 ? 1 + wave / 2 : 0;
            int f = wave >= 3 ? 1 + (wave - 3) / 2 : 0;
            if (mode.equals("QUICK FIGHT")) g = 3 + wave;
            for (int i = 0; i < g; i++) queue.add(new int[]{0, 25 + i * 20});
            for (int i = 0; i < d; i++) queue.add(new int[]{1, 70 + i * 55});
            for (int i = 0; i < f; i++) queue.add(new int[]{2, 90 + i * 60});
        }

        Enemy spawnEnemy(int type) {
            Enemy e = new Enemy();
            e.type = type;
            double mul = DIFF_MUL[diff] * (1 + level * 0.18) * (1 + wave * 0.08);
            if (type == 0) {
                e.w = 30;
                e.h = 44;
                e.hp = 40 * mul;
                e.speed = Math.min(4.2, 1.9 + wave * 0.07);
                e.dmg = 8 + diff * 3;
                e.c = RED;
            } else if (type == 1) {
                e.w = 34;
                e.h = 40;
                e.hp = 72 * mul;
                e.speed = Math.min(4.6, 2.3 + wave * 0.06);
                e.dmg = 13 + diff * 3;
                e.c = MAGENTA;
            } else if (type == 2) {
                e.w = 36;
                e.h = 28;
                e.hp = 46 * mul;
                e.speed = Math.min(4.4, 2.2 + wave * 0.06);
                e.dmg = 10 + diff * 3;
                e.c = GOLD;
            } else if (type == 3) {
                e.w = 86;
                e.h = 96;
                e.hp = 420 * mul;
                e.speed = 2.0;
                e.dmg = 22 + diff * 5;
                e.c = new Color(255, 40, 110);
            } else {
                e.w = 40;
                e.h = 56;
                e.hp = 100000;
                e.speed = 0;
                e.dmg = 0;
                e.c = new Color(120, 200, 255);
            }
            e.maxHp = e.hp;
            int side = R.nextInt(type == 2 ? 3 : 3);
            if (side == 0) {
                e.x = 60 + R.nextInt(GW - 160);
                e.y = -60;
            } else if (side == 1) {
                e.x = -50;
                e.y = 80 + R.nextInt(220);
            } else {
                e.x = GW + 10;
                e.y = 80 + R.nextInt(220);
            }
            if (type == 3) {
                e.x = face > 0 ? GW + 40 : -120;
                e.y = groundY() - e.h - 60;
                shake = 12;
                Sound.beep(120, 300, 0.3);
            }
            if (type == 4) {
                e.x = 150 + R.nextInt(GW - 300);
                e.y = groundY() - e.h;
            }
            es.add(e);
            return e;
        }

        void updateEnemy(Enemy e, double slow) {
            double dx = px - e.x;
            double dy = py - e.y;
            e.timer++;
            if (e.flash > 0) e.flash--;

            if (e.type == 4) return;

            if (e.type == 0) {
                e.dir = dx > 0 ? 1 : -1;
                e.vx = e.dir * e.speed;
                e.vy += 0.7;
                e.x += e.vx * slow;
                e.y += e.vy * slow;
                landEnemy(e);
            } else if (e.type == 1) {
                if (e.state == 0) {
                    e.dir = dx > 0 ? 1 : -1;
                    e.vx = e.dir * e.speed * 0.6;
                    e.vy += 0.7;
                    if (e.timer % 95 == 0 && Math.abs(dx) < 520) {
                        e.state = 1;
                        e.atk = 26;
                    }
                } else if (e.state == 1) {
                    e.vx *= 0.7;
                    e.vy += 0.7;
                    e.atk--;
                    if (e.atk <= 0) {
                        e.state = 2;
                        e.atk = 34;
                        e.flash = 8;
                        Sound.beep(240, 60, 0.16);
                    }
                } else {
                    e.vx = e.dir * e.speed * 4.4;
                    e.vy += 0.4;
                    e.atk--;
                    if (e.atk <= 0) {
                        e.state = 0;
                        e.timer = 40;
                    }
                }
                e.x += e.vx * slow;
                e.y += e.vy * slow;
                landEnemy(e);
            } else if (e.type == 2) {
                e.dir = dx > 0 ? 1 : -1;
                e.x += e.dir * e.speed * slow;
                e.y += Math.sin(e.timer * 0.06) * 2.2 * slow;
                double ty = py - 40 + Math.sin(e.timer * 0.05) * 40;
                e.y += (ty - e.y) * 0.02 * slow;
                e.x = Math.max(10, Math.min(GW - e.w - 10, e.x));
                e.y = Math.max(70, Math.min(groundY() - e.h, e.y));
            } else if (e.type == 3) {
                e.dir = dx > 0 ? 1 : -1;
                if (e.state == 0) {
                    e.vx = e.dir * e.speed;
                    e.vy += 0.7;
                    if (e.timer % 170 == 0) {
                        e.state = 1;
                        e.atk = 32;
                    }
                    if (e.timer % 260 == 0 && es.size() < 8) {
                        spawnEnemy(0);
                        spawnEnemy(0);
                        shake = 8;
                    }
                } else if (e.state == 1) {
                    e.vx *= 0.75;
                    e.vy += 0.7;
                    e.atk--;
                    if (e.atk <= 0) {
                        e.state = 2;
                        e.atk = 55;
                        e.flash = 10;
                        Sound.beep(90, 220, 0.3);
                    }
                } else {
                    e.vx = e.dir * 11;
                    e.vy += 0.4;
                    e.atk--;
                    shake = Math.max(shake, 3);
                    if (e.atk <= 0) e.state = 0;
                }
                e.x += e.vx * slow;
                e.y += e.vy * slow;
                landEnemy(e);
            }

            if (e.x < -80) e.x = -80;
            if (e.x + e.w > GW + 80) e.x = GW + 80 - e.w;

            if (e.dmg > 0 && new Rectangle2D.Double(px, py, pw, ph).intersects(e.x, e.y, e.w, e.h)) {
                hurtPlayer(e.dmg, e.x + e.w / 2);
            }
        }

        void landEnemy(Enemy e) {
            if (e.y + e.h >= groundY()) {
                e.y = groundY() - e.h;
                e.vy = 0;
            }
        }

        void hurtEnemy(Enemy e, double dmg, int dir) {
            e.hp -= dmg;
            e.flash = 6;
            e.x += dir * (e.type == 3 ? 4 : 14);
            puff(e.x + e.w / 2, e.y + e.h / 2, e.c, 8);
            Sound.beep(e.type == 3 ? 200 : 340, 35, 0.14);
        }

        void killEnemy(Enemy e) {
            es.remove(e);
            if (e.type == 4) {
                queue.add(new int[]{4, 40});
                puff(e.x + e.w / 2, e.y + e.h / 2, e.c, 14);
                return;
            }
            kills++;
            combo++;
            comboT = 130;
            int base = e.type == 3 ? 300 : e.type == 1 ? 40 : e.type == 2 ? 35 : 25;
            double mult = Math.min(3, 1 + combo * 0.12);
            score += (int) (base * mult);
            int coinsGot = e.type == 3 ? 15 : 1 + R.nextInt(3);
            runCoins += coinsGot;
            shake = Math.max(shake, e.type == 3 ? 16 : 5);
            int n = (e.type == 3 ? 40 : 16) * Math.max(1, particles);
            for (int i = 0; i < n; i++) {
                Part p = new Part();
                p.x = e.x + e.w / 2;
                p.y = e.y + e.h / 2;
                p.vx = (R.nextDouble() - 0.5) * 11;
                p.vy = (R.nextDouble() - 0.5) * 11 - 2;
                p.life = p.maxLife = 30 + R.nextInt(20);
                p.size = 2 + R.nextDouble() * 4;
                p.c = e.c;
                ps.add(p);
            }
            Sound.beep(e.type == 3 ? 140 : 620, e.type == 3 ? 350 : 70, 0.2);
            if (e.type == 3) Sound.beep(280, 400, 0.2);
            if (R.nextInt(100) < 30) {
                Power u = new Power();
                u.x = e.x + e.w / 2;
                u.y = e.y + e.h / 2;
                u.vy = -3;
                int r = R.nextInt(100);
                u.type = r < 26 ? 0 : r < 50 ? 1 : r < 70 ? 2 : r < 88 ? 3 : 4;
                ups.add(u);
            }
        }

        void applyPower(int type) {
            if (type == 0) {
                hp = Math.min(maxHp, hp + 25);
                Sound.beep(980, 90, 0.22);
            } else if (type == 1) {
                rageT = 480;
                Sound.beep(420, 120, 0.22);
            } else if (type == 2) {
                shield++;
                Sound.beep(760, 110, 0.22);
            } else if (type == 3) {
                slowT = 300;
                Sound.beep(560, 130, 0.22);
            } else {
                runCoins += 12;
                Sound.beep(1150, 80, 0.22);
            }
            for (int i = 0; i < 12 * Math.max(1, particles); i++) {
                Part p = new Part();
                p.x = px + pw / 2;
                p.y = py + ph / 2;
                p.vx = (R.nextDouble() - 0.5) * 8;
                p.vy = (R.nextDouble() - 0.5) * 8;
                p.life = p.maxLife = 26;
                p.size = 3;
                p.c = type == 0 ? LIME : type == 1 ? RED : type == 2 ? CYAN : GOLD;
                ps.add(p);
            }
        }

        void hurtPlayer(double dmg, double srcX) {
            if (invuln > 0 || dashCd > dashCdMax - 12 || ended) return;
            if (isTraining) {
                invuln = 60;
                return;
            }
            if (shield > 0) {
                shield--;
                invuln = 70;
                shake = 8;
                puff(px + pw / 2, py + ph / 2, CYAN, 16);
                Sound.beep(820, 90, 0.22);
                return;
            }
            hp -= dmg;
            invuln = 75;
            combo = 0;
            shake = 12;
            vx = (px + pw / 2 < srcX ? -1 : 1) * 8;
            vy = -5;
            puff(px + pw / 2, py + ph / 2, RED, 16);
            Sound.beep(180, 130, 0.26);
            if (hp <= 0) {
                hp = 0;
                finish(false);
            }
        }

        void finish(boolean win) {
            if (ended) return;
            ended = true;
            tm.stop();
            held.clear();
            int bonus = win ? 120 : 0;
            gained = runCoins + score / 25 + bonus;
            coins += gained;
            if (win && mode.equals("CAMPAIGN") && level + 1 < 5) {
                unlockedLevels = Math.max(unlockedLevels, level + 2);
            }
            save();
            if (!saved) {
                saved = true;
                saveScore(score, mode, wave);
            }
            go(win ? "victory" : "gameover");
        }

        void puff(double x, double y, Color c, int n) {
            for (int i = 0; i < n * Math.max(1, particles); i++) {
                Part p = new Part();
                p.x = x;
                p.y = y;
                p.vx = (R.nextDouble() - 0.5) * 7;
                p.vy = (R.nextDouble() - 0.5) * 7;
                p.life = p.maxLife = 20 + R.nextInt(12);
                p.size = 2 + R.nextDouble() * 3;
                p.c = c;
                ps.add(p);
            }
        }

        public void actionPerformed(ActionEvent e) {
            if ("play".equals(screen)) {
                for (int i = 0; i < queue.size(); i++) {
                    int[] s = queue.get(i);
                    s[1]--;
                    if (s[1] <= 0) {
                        spawnEnemy(s[0]);
                        queue.remove(i);
                        break;
                    }
                }
                step();
            }
            repaint();
        }

        public void mousePressed(MouseEvent e) {
            if (!"play".equals(screen)) return;
            if (SwingUtilities.isLeftMouseButton(e)) tryAttack();
            else if (SwingUtilities.isRightMouseButton(e)) tryDash();
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawBackground(g);
            int sx = 0, sy = 0;
            if (shakeOn && shake > 0) {
                sx = R.nextInt(shake * 2 + 1) - shake;
                sy = R.nextInt(shake * 2 + 1) - shake;
            }
            g.translate(sx, sy);
            drawPlatforms(g);
            drawPowers(g);
            drawEnemies(g);
            drawPlayer(g);
            drawParts(g);
            g.translate(-sx, -sy);
            drawHud(g);
            drawBanner(g);
        }

        void drawBackground(Graphics2D g) {
            g.setPaint(new GradientPaint(0, 0, LV_TOP[level], 0, GH, LV_BOT[level]));
            g.fillRect(0, 0, GW, GH);
            Random r = new Random(level * 13 + 1);
            for (int i = 0; i < 60; i++) {
                int x = r.nextInt(GW);
                int y = r.nextInt(groundY() - 150);
                g.setColor(new Color(255, 255, 255, 30 + r.nextInt(60)));
                g.fillRect(x, y, 2, 2);
            }
            for (int i = 0; i < skyline.length; i++) {
                int x = skyline[i][0];
                int h = skyline[i][1];
                g.setColor(new Color(3, 6, 16, 235));
                g.fillRect(x, groundY() - h, 30, h);
                g.setColor(new Color(LV_ACC[level].getRed(), LV_ACC[level].getGreen(), LV_ACC[level].getBlue(), 40));
                g.drawRect(x, groundY() - h, 30, h);
                g.setColor(new Color(LV_ACC[level].getRed(), LV_ACC[level].getGreen(), LV_ACC[level].getBlue(), 90));
                for (int wy = groundY() - h + 8; wy < groundY() - 10; wy += 16) {
                    if ((wy + x + skyline[i][2]) % 3 != 0) g.fillRect(x + 6, wy, 5, 6);
                }
            }
            g.setColor(new Color(6, 10, 22));
            g.fillRect(0, groundY(), GW, GH - groundY());
            neon(g, new Line2D.Double(0, groundY(), GW, groundY()), LV_ACC[level], 3f);
            g.setColor(new Color(LV_ACC[level].getRed(), LV_ACC[level].getGreen(), LV_ACC[level].getBlue(), 35));
            for (int x = 0; x < GW; x += 50) g.drawLine(x, groundY() + 8, x, GH);
            for (int y = groundY() + 14; y < GH; y += 14) g.drawLine(0, y, GW, y);
        }

        void drawPlatforms(Graphics2D g) {
            for (Plat pl : plats) {
                RoundRectangle2D r = new RoundRectangle2D.Double(pl.x, pl.y, pl.w, pl.h, 10, 10);
                g.setColor(new Color(8, 14, 30, 240));
                g.fill(r);
                neon(g, r, LV_ACC[level], 2.5f);
            }
        }

        void drawPowers(Graphics2D g) {
            for (Power u : ups) {
                double s = 13 + Math.sin(u.t * 0.15) * 2;
                Color c = u.type == 0 ? LIME : u.type == 1 ? RED : u.type == 2 ? CYAN : u.type == 3 ? MAGENTA : GOLD;
                double a = u.t * 0.06;
                AffineTransform old = g.getTransform();
                g.rotate(a, u.x, u.y);
                Path2D diamond = new Path2D.Double();
                diamond.moveTo(u.x, u.y - s);
                diamond.lineTo(u.x + s, u.y);
                diamond.lineTo(u.x, u.y + s);
                diamond.lineTo(u.x - s, u.y);
                diamond.closePath();
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
                g.fill(diamond);
                neon(g, diamond, c, 2.5f);
                g.setTransform(old);
                g.setFont(font(14, true));
                g.setColor(Color.WHITE);
                String ch = u.type == 0 ? "H" : u.type == 1 ? "R" : u.type == 2 ? "S" : u.type == 3 ? "T" : "$";
                FontMetrics fm = g.getFontMetrics();
                g.drawString(ch, (float) (u.x - fm.stringWidth(ch) / 2), (float) (u.y + fm.getAscent() / 2 - 2));
            }
        }

        void drawEnemies(Graphics2D g) {
            for (Enemy e : es) {
                Color c = e.flash > 0 ? Color.WHITE : e.c;
                if (e.type == 0) {
                    RoundRectangle2D b = new RoundRectangle2D.Double(e.x, e.y, e.w, e.h, 10, 10);
                    g.setColor(new Color(16, 6, 12, 245));
                    g.fill(b);
                    neon(g, b, c, 2.5f);
                    g.setColor(Color.WHITE);
                    int ex = e.dir > 0 ? (int) e.x + 17 : (int) e.x + 5;
                    g.fillRect(ex, (int) e.y + 12, 6, 3);
                    g.fillRect(ex + (e.dir > 0 ? 8 : -8), (int) e.y + 12, 6, 3);
                } else if (e.type == 1) {
                    Path2D d = new Path2D.Double();
                    d.moveTo(e.x + e.w / 2, e.y);
                    d.lineTo(e.x + e.w, e.y + e.h / 2);
                    d.lineTo(e.x + e.w / 2, e.y + e.h);
                    d.lineTo(e.x, e.y + e.h / 2);
                    d.closePath();
                    g.setColor(new Color(20, 4, 22, 245));
                    g.fill(d);
                    neon(g, d, c, 3f);
                    if (e.state == 1) {
                        g.setColor(new Color(255, 255, 255, 90 + (int) (Math.sin(t * 0.5) * 70)));
                        g.fill(d);
                    }
                } else if (e.type == 2) {
                    Ellipse2D b = new Ellipse2D.Double(e.x, e.y, e.w, e.h);
                    g.setColor(new Color(20, 14, 4, 245));
                    g.fill(b);
                    neon(g, b, c, 2.5f);
                    neon(g, new Arc2D.Double(e.x - 14, e.y - 6, 26, 40, 80, 150, Arc2D.OPEN), c, 2f);
                    neon(g, new Arc2D.Double(e.x + e.w - 12, e.y - 6, 26, 40, -30, 150, Arc2D.OPEN), c, 2f);
                    g.setColor(Color.WHITE);
                    g.fillRect((int) e.x + (e.dir > 0 ? 22 : 8), (int) e.y + 10, 7, 3);
                } else if (e.type == 3) {
                    RoundRectangle2D b = new RoundRectangle2D.Double(e.x, e.y, e.w, e.h, 22, 22);
                    g.setColor(new Color(24, 2, 14, 250));
                    g.fill(b);
                    neon(g, b, c, 4f);
                    Path2D horns = new Path2D.Double();
                    horns.moveTo(e.x + 6, e.y + 6);
                    horns.lineTo(e.x + 22, e.y - 22);
                    horns.lineTo(e.x + 32, e.y + 8);
                    horns.moveTo(e.x + e.w - 6, e.y + 6);
                    horns.lineTo(e.x + e.w - 22, e.y - 22);
                    horns.lineTo(e.x + e.w - 32, e.y + 8);
                    neon(g, horns, c, 3f);
                    g.setColor(Color.WHITE);
                    int ex = e.dir > 0 ? (int) e.x + 52 : (int) e.x + 14;
                    g.fillRect(ex, (int) e.y + 34, 14, 6);
                    g.fillRect(ex + (e.dir > 0 ? 16 : -16), (int) e.y + 34, 14, 6);
                    if (e.state == 1) {
                        g.setColor(new Color(255, 60, 60, 60 + (int) (Math.sin(t * 0.4) * 50)));
                        g.fill(b);
                    }
                } else {
                    RoundRectangle2D b = new RoundRectangle2D.Double(e.x, e.y, e.w, e.h, 12, 12);
                    g.setColor(new Color(8, 18, 30, 245));
                    g.fill(b);
                    neon(g, b, c, 2.5f);
                    g.setColor(c);
                    g.drawOval((int) e.x + 8, (int) e.y + 14, 24, 24);
                    g.drawOval((int) e.x + 14, (int) e.y + 20, 12, 12);
                }
                if (e.type != 4 && e.hp < e.maxHp) {
                    double bw = Math.max(40, e.w);
                    double bx = e.x + e.w / 2 - bw / 2;
                    double by = e.y - 12;
                    g.setColor(new Color(0, 0, 0, 160));
                    g.fillRect((int) bx, (int) by, (int) bw, 6);
                    g.setColor(c);
                    g.fillRect((int) bx, (int) by, (int) (bw * Math.max(0, e.hp) / e.maxHp), 6);
                }
                if (e.type == 3) {
                    g.setFont(font(16, true));
                    g.setColor(RED);
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString("ONI OVERLORD", (float) (e.x + e.w / 2 - fm.stringWidth("ONI OVERLORD") / 2), (float) (e.y - 22));
                }
            }
        }

        void drawPlayer(Graphics2D g) {
            Color c = CH_COL[equipped];
            for (int i = 0; i < trail.size(); i++) {
                double[] tr = trail.get(i);
                double a = (i + 1) * 9;
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) a));
                g.fillRoundRect((int) tr[0], (int) tr[1], (int) pw, (int) ph, 12, 12);
            }
            boolean blink = invuln > 0 && (invuln / 4) % 2 == 0;
            if (blink) return;

            g.setColor(new Color(0, 0, 0, 100));
            g.fill(new Ellipse2D.Double(px - 8, groundY() + 2, pw + 16, 9));

            g.setColor(new Color(10, 14, 34));
            g.fill(new Rectangle2D.Double(px + 3, py + ph - 15, 10, 15));
            g.fill(new Rectangle2D.Double(px + pw - 13, py + ph - 15, 10, 15));

            RoundRectangle2D body = new RoundRectangle2D.Double(px, py + 13, pw, ph - 17, 12, 12);
            g.setColor(new Color(10, 15, 36));
            g.fill(body);
            neon(g, body, c, 2.5f);

            Ellipse2D head = new Ellipse2D.Double(px - 3, py - 5, pw + 6, 26);
            g.setColor(new Color(10, 15, 36));
            g.fill(head);
            neon(g, head, c, 2.5f);

            g.setColor(new Color(0, 0, 0, 200));
            g.fill(new Rectangle2D.Double(px - 3, py + 3, pw + 6, 9));
            Color eye = rageT > 0 ? RED : Color.WHITE;
            g.setColor(eye);
            int ey = (int) py + 5;
            g.fillRect((int) px + (face > 0 ? 15 : 6), ey, 7, 4);
            g.fillRect((int) px + (face > 0 ? 24 : 15), ey, 7, 4);

            double bx = face > 0 ? px + 2 : px + pw - 2;
            double w1 = Math.sin(t * 0.16) * 7;
            double w2 = Math.sin(t * 0.16 + 1.4) * 7;
            neon(g, new Line2D.Double(bx, py + 2, bx - face * 26, py - 4 + w1), c, 2.5f);
            neon(g, new Line2D.Double(bx, py + 5, bx - face * 22, py + 8 + w2), c, 2.5f);

            if (atkT <= 0) {
                double hx = px + pw / 2 + face * 6;
                neon(g, new Line2D.Double(hx, py + 24, hx + face * 20, py + 46), ICE, 3f);
            } else {
                double prog = 1 - atkT / 12.0;
                double start = face > 0 ? -85 + prog * 55 : 110 - prog * 55;
                Arc2D arc = new Arc2D.Double(px - 34, py - 20, pw + 68, ph + 40, start, 130, Arc2D.OPEN);
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (40 + 170 * (1 - prog))));
                g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(arc);
                neon(g, arc, Color.WHITE, 3f);
            }

            if (shield > 0) {
                Ellipse2D sh = new Ellipse2D.Double(px - 12, py - 12, pw + 24, ph + 24);
                neon(g, sh, CYAN, 2f);
                g.setFont(font(13, true));
                g.setColor(CYAN);
                g.drawString("x" + shield, (float) (px + pw + 6), (float) (py - 4));
            }
            if (combo > 1) {
                g.setFont(font(18, true));
                g.setColor(GOLD);
                String s = "COMBO x" + combo;
                FontMetrics fm = g.getFontMetrics();
                g.drawString(s, (float) (px + pw / 2 - fm.stringWidth(s) / 2), (float) (py - 24));
            }
        }

        void drawParts(Graphics2D g) {
            for (Part p : ps) {
                double a = Math.max(0, p.life / p.maxLife);
                g.setColor(new Color(p.c.getRed(), p.c.getGreen(), p.c.getBlue(), (int) (255 * a)));
                g.fill(new Ellipse2D.Double(p.x, p.y, p.size, p.size));
            }
        }

        void drawHud(Graphics2D g) {
            g.setFont(font(15, true));
            RoundRectangle2D barBg = new RoundRectangle2D.Double(18, 16, 260, 26, 12, 12);
            g.setColor(new Color(0, 0, 0, 170));
            g.fill(barBg);
            neon(g, barBg, hp > maxHp * 0.3 ? LIME : RED, 2f);
            double w = 252 * Math.max(0, hp) / maxHp;
            if (w > 0) {
                g.setColor(hp > maxHp * 0.3 ? LIME : RED);
                g.fillRoundRect(22, 20, (int) w, 18, 9, 9);
            }
            g.setColor(Color.WHITE);
            g.drawString("HP " + (int) Math.max(0, hp) + " / " + (int) maxHp, 26, 35);
            g.setColor(CYAN);
            g.drawString("DASH", 18, 62);
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(70, 50, 208, 14);
            double dc = 1 - Math.max(0, dashCd) / dashCdMax;
            g.setColor(dc >= 1 ? CYAN : new Color(0, 140, 180));
            g.fillRect(70, 50, (int) (208 * Math.max(0, Math.min(1, dc))), 14);

            g.setFont(font(17, true));
            String center;
            if (isTime) {
                int sec = (int) Math.ceil(timeLeft);
                center = "TIME LEFT  " + sec + "s";
            } else if (totalWaves > 0) {
                center = "WAVE " + wave + " / " + totalWaves;
            } else {
                center = "WAVE " + wave;
            }
            g.setColor(GOLD);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(center, GW / 2 - fm.stringWidth(center) / 2, 32);

            g.setFont(font(16, true));
            String right = "SCORE " + score;
            g.setColor(CYAN);
            g.drawString(right, GW - 30 - g.getFontMetrics().stringWidth(right), 26);
            String rc = "COINS " + runCoins;
            g.setColor(GOLD);
            g.drawString(rc, GW - 30 - g.getFontMetrics().stringWidth(rc), 48);
            String kl = "KILLS " + kills;
            g.setColor(DIM);
            g.drawString(kl, GW - 30 - g.getFontMetrics().stringWidth(kl), 70);

            int bx = 18;
            if (rageT > 0) {
                g.setColor(RED);
                g.drawString("RAGE " + (int) (rageT / 60 + 1) + "s", bx, 92);
                bx += 110;
            }
            if (slowT > 0) {
                g.setColor(MAGENTA);
                g.drawString("TIME SLOW " + (int) (slowT / 60 + 1) + "s", bx, 92);
                bx += 150;
            }
            if (shield > 0) {
                g.setColor(CYAN);
                g.drawString("SHIELD x" + shield, bx, 92);
            }
            g.setColor(DIM);
            g.drawString(mode + "  |  " + LV_NAMES[level] + "  |  " + DIFF_NAMES[diff], 18, GH - 14);
        }

        void drawBanner(Graphics2D g) {
            if (waveBanner <= 0 || ended) return;
            double a = Math.min(1, waveBanner / 30.0);
            String s;
            if (waveDelay > 0 && wave == 0) s = "GET READY";
            else if (totalWaves > 0 && wave % 5 == 0 && wave > 0) s = "!! BOSS WAVE " + wave + " !!";
            else s = "WAVE " + wave;
            g.setFont(font(52, true));
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(s);
            g.setColor(new Color(0, 0, 0, (int) (140 * a)));
            g.fillRoundRect(GW / 2 - w / 2 - 30, GH / 2 - 70, w + 60, 80, 20, 20);
            for (int i = 6; i >= 1; i--) {
                g.setColor(new Color(0, 240, 255, (int) (26 * a)));
                g.drawString(s, GW / 2 - w / 2 + i, GH / 2 - 10 + i);
            }
            g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), (int) (255 * a)));
            g.drawString(s, GW / 2 - w / 2, GH / 2 - 10);
        }
    }
}
