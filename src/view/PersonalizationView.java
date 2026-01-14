package view;

import controller.AppController;
import model.SysData;
import util.BackgroundMusic;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class PersonalizationView extends BaseGameFrame {

    private final SysData sys;

    private static final Color DEFAULT_GOLD = new Color(255, 204, 0);

    private static final Map<String, Color> ACCENTS = new LinkedHashMap<>();
    static {
        ACCENTS.put("Golden (Default)", DEFAULT_GOLD);
        ACCENTS.put("Royal Blue",       new Color(0x3B82F6));
        ACCENTS.put("Emerald Green",    new Color(0x10B981));
        ACCENTS.put("Purple",           new Color(0x8B5CF6));
        ACCENTS.put("Crimson",          new Color(0xEF4444));
        ACCENTS.put("Cyan / Aqua",      new Color(0x22D3EE));
    }

    // theme.properties fallback (if GameAssets helpers are unavailable)
    private static final String THEME_FILE = "theme.properties";
    private static final String KEY_BG = "backgroundKey"; // BG1 / BG2 / BG3 options

    // UI state
    private final ButtonGroup accentGroup = new ButtonGroup();
    private final JLabel titleLabel = new JLabel("Personalization");
    private Color pendingAccent;

    // Background selection
    private String pendingBackgroundKey = "BG1";
    private final Map<String, BgChoiceButton> bgButtons = new LinkedHashMap<>();
    private final Map<String, JLabel> bgThumbLabels = new LinkedHashMap<>();
    private final Map<String, ImageIcon> thumbCache = new LinkedHashMap<>();

    // Preview
    private JLabel miniTitle;
    private JPanel previewCard;
    private PreviewBackgroundPanel previewBgPanel;

    // Tabs switch the left column (Color / Background)
    private final CardLayout leftCards = new CardLayout();
    private final JPanel leftCardHost = new JPanel(leftCards);

    public PersonalizationView(AppController app) {
        super(app, "Personalization");
        this.sys = SysData.getInstance();

        // Load last saved theme key (BG1/BG2/BG3)
        pendingBackgroundKey = safeLoadBackgroundKey();

        // Screen background image (this is the PERSONALIZATION screen background)
        Image bgImage = GameAssets.PERSON_BACKGROUND != null
                ? GameAssets.PERSON_BACKGROUND
                : GameAssets.MAIN_BACKGROUND;

        BackgroundPanel bgPanel = new BackgroundPanel(bgImage);
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);

        // ============================================================
        // TOP: Title + subtitle (always centered)
        // ============================================================
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 10, 24));

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(UIStyles.ACCENT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 80));
        topPanel.add(titleLabel);

        topPanel.add(Box.createVerticalStrut(8));

        JLabel sub = new JLabel("Choose your accent color or background theme. It will remain until you change it again.");
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setForeground(new Color(255, 255, 255, 220));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        topPanel.add(sub);

        bgPanel.add(topPanel, BorderLayout.NORTH);

        // ============================================================
        // Initial accent
        // ============================================================
        Color current = sys.getAccentColor();
        pendingAccent = current;

       
        JPanel centerWrap = new JPanel();
        centerWrap.setOpaque(false);
        centerWrap.setLayout(new BoxLayout(centerWrap, BoxLayout.Y_AXIS));

        JPanel tabsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tabsRow.setOpaque(false);

        // fixed container 
        JPanel tabsWidthBox = new JPanel(new BorderLayout());
        tabsWidthBox.setOpaque(false);
        tabsWidthBox.setPreferredSize(new Dimension(1200, 60));
        tabsWidthBox.setMaximumSize(new Dimension(1200, 60));
        tabsWidthBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        tabsWidthBox.add(tabsRow, BorderLayout.WEST);

        tabsRow.add(buildTabs());

        centerWrap.add(tabsWidthBox);

        // ---- Big main card  ----
        JPanel mainCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainCard.setOpaque(false);
        mainCard.setBorder(BorderFactory.createEmptyBorder(28, 40, 28, 40));
        mainCard.setPreferredSize(new Dimension(1200, 520));
        mainCard.setMaximumSize(new Dimension(1200, 520));
        mainCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel columns = new JPanel(new GridLayout(1, 2, 70, 0));
        columns.setOpaque(false);

        // LEFT: cards (Color / Background)
        leftCardHost.setOpaque(false);
        leftCardHost.add(buildLeftColorColumn(current), "COLOR");
        leftCardHost.add(buildLeftBackgroundColumn(), "BG");
        leftCardHost.add(buildLeftMusicColumn(), "MUSIC");
        leftCards.show(leftCardHost, "COLOR");


        // RIGHT: live preview
        JPanel rightPreview = buildRightPreviewColumn();

        columns.add(leftCardHost);
        columns.add(rightPreview);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        mainCard.add(columns, gc);

        centerWrap.add(mainCard);

        // Center it on the screen
        JPanel centerOuter = new JPanel(new GridBagLayout());
        centerOuter.setOpaque(false);
        centerOuter.add(centerWrap);

        bgPanel.add(centerOuter, BorderLayout.CENTER);

        // ============================================================
        // BOTTOM: Back / Save
        // ============================================================
        JPanel bottomButtons = new JPanel();
        bottomButtons.setOpaque(false);
        bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.X_AXIS));
        bottomButtons.setMaximumSize(new Dimension(1200, 120));
        bottomButtons.setBorder(BorderFactory.createEmptyBorder(10, 90, 0, 90));

        ButtonStyled backBtn = new ButtonStyled("Back");
        ButtonStyled saveBtn = new ButtonStyled("Save");

        bottomButtons.add(Box.createHorizontalGlue());
        bottomButtons.add(backBtn);
        bottomButtons.add(Box.createHorizontalStrut(280));
        bottomButtons.add(saveBtn);
        bottomButtons.add(Box.createHorizontalGlue());

        JPanel southWrap = new JPanel(new BorderLayout());
        southWrap.setOpaque(false);
        southWrap.setBorder(BorderFactory.createEmptyBorder(0, 0, 55, 0));
        southWrap.add(bottomButtons, BorderLayout.NORTH);
        bgPanel.add(southWrap, BorderLayout.SOUTH);

        // Navigation
        backBtn.addActionListener(e -> {
            dispose();
            app.showMainMenu();
        });

        // Save applies accent immediately, saves background key, reloads assets, returns to main menu
        saveBtn.addActionListener(e -> {
            saveAccentTheme();
            safeSaveBackgroundKey(pendingBackgroundKey);
            GameAssets.reloadThemeBackgrounds();
            dispose();
            app.showMainMenu();
        });

        // Initial preview
        updatePreviewAccent(current);

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

        // async loads (thumbnails + preview background)
        loadBackgroundThumbnailsAsync();
        loadPreviewBackgroundAsync(pendingBackgroundKey);
    }

    // =================================================================================
    // Tabs (switch left card: COLOR / BG)
    // =================================================================================
    private JComponent buildTabs() {
        TabButton colorTab = new TabButton("Color");
        TabButton bgTab    = new TabButton("Background");
        TabButton musicTab = new TabButton("Music");

        ButtonGroup grp = new ButtonGroup();
        grp.add(colorTab);
        grp.add(bgTab);
        grp.add(musicTab);

        styleTab(colorTab);
        styleTab(bgTab);
        styleTab(musicTab);

        colorTab.setSelected(true);

        colorTab.addActionListener(e ->
                leftCards.show(leftCardHost, "COLOR")
        );

        bgTab.addActionListener(e -> {
            leftCards.show(leftCardHost, "BG");
            applyBackgroundButtonVisuals();
        });

        musicTab.addActionListener(e ->
                leftCards.show(leftCardHost, "MUSIC")
        );

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.add(colorTab);
        row.add(bgTab);
        row.add(musicTab);

        return row;
    }



    private static class TabButton extends JToggleButton {

        private static final Color TAB_COLOR = new Color(0, 0, 0, 140);

        public TabButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected()) {
                // Selected → filled, no border
                g2.setColor(TAB_COLOR);
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                // Unselected → border only
                g2.setColor(TAB_COLOR);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    
    private void styleTab(JToggleButton b) {
        b.setPreferredSize(new Dimension(300, 60));
        b.setFont(new Font("Segoe UI", Font.BOLD, 32));
        b.setForeground(Color.WHITE);

        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false); 
        b.setOpaque(false);           

        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }



    // =================================================================================
    // LEFT: Color Column
    // =================================================================================
    private JPanel buildLeftColorColumn(Color current) {
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        JLabel chooseLbl = new JLabel("Choose Accent Color", SwingConstants.LEFT);
        chooseLbl.setForeground(Color.WHITE);
        chooseLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        chooseLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCol.add(chooseLbl);
        leftCol.add(Box.createVerticalStrut(18));

        JPanel tilesGrid = new JPanel(new GridLayout(0, 2, 18, 18));
        tilesGrid.setOpaque(false);
        tilesGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Map.Entry<String, Color> entry : ACCENTS.entrySet()) {
            String name = entry.getKey();
            Color color = entry.getValue();

            ColorTileRadio rb = new ColorTileRadio(name, color);
            if (colorsClose(color, current)) rb.setSelected(true);

            rb.addActionListener(e -> updatePreviewAccent(color));

            accentGroup.add(rb);
            tilesGrid.add(rb);
        }

        leftCol.add(tilesGrid);
        return leftCol;
    }

    // =================================================================================
    // LEFT: Background Column
    // =================================================================================
    private JPanel buildLeftBackgroundColumn() {
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        JLabel chooseLbl = new JLabel("Choose Background", SwingConstants.LEFT);
        chooseLbl.setForeground(Color.WHITE);
        chooseLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        chooseLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCol.add(chooseLbl);
        leftCol.add(Box.createVerticalStrut(18));

        JPanel list = new JPanel(new GridLayout(3, 1, 0, 16));
        list.setOpaque(false);
        list.setAlignmentX(Component.LEFT_ALIGNMENT);

        list.add(makeBackgroundRow("Background 1", "BG1"));
        list.add(makeBackgroundRow("Background 2", "BG2"));
        list.add(makeBackgroundRow("Background 3", "BG3"));

        leftCol.add(list);

        applyBackgroundButtonVisuals();
        return leftCol;
    }

    private JComponent makeBackgroundRow(String label, String key) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);

        // thumbnail box
        int thumbW = 140;
        int thumbH = 78;

        JLabel pic = new JLabel("", SwingConstants.CENTER);
        pic.setPreferredSize(new Dimension(thumbW, thumbH));
        pic.setMinimumSize(new Dimension(thumbW, thumbH));
        pic.setOpaque(true);
        pic.setBackground(new Color(10, 12, 18, 220));
        pic.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 70), 1));
        bgThumbLabels.put(key, pic);

        // background button 
        BgChoiceButton btn = new BgChoiceButton(label);
        btn.setPreferredSize(new Dimension(420, thumbH));
        btn.setMinimumSize(new Dimension(420, thumbH));
        bgButtons.put(key, btn);

        btn.addActionListener(e -> {
            pendingBackgroundKey = key;
            applyBackgroundButtonVisuals();
            loadPreviewBackgroundAsync(key);
        });

        row.add(pic, BorderLayout.WEST);
        row.add(btn, BorderLayout.CENTER);
        return row;
    }


    private void applyBackgroundButtonVisuals() {
        for (Map.Entry<String, BgChoiceButton> e : bgButtons.entrySet()) {
            boolean selected = e.getKey().equalsIgnoreCase(pendingBackgroundKey);
            e.getValue().setSelectedVisual(selected);
        }
    }

    // =================================================================================
    // RIGHT: Preview Column
    // =================================================================================
    private JPanel buildRightPreviewColumn() {
        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));

        JLabel prevTitle = new JLabel("Live Preview", SwingConstants.LEFT);
        prevTitle.setForeground(Color.WHITE);
        prevTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        prevTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightCol.add(prevTitle);
        rightCol.add(Box.createVerticalStrut(18));

        previewBgPanel = new PreviewBackgroundPanel();
        previewBgPanel.setOpaque(false);
        previewBgPanel.setPreferredSize(new Dimension(520, 240));
        previewBgPanel.setMaximumSize(new Dimension(520, 240));
        previewBgPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        previewBgPanel.setLayout(new GridBagLayout());

        previewCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 165));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setStroke(new BasicStroke(3f));
                g2.setColor(pendingAccent);
                g2.drawRect(2, 2, getWidth() - 4, getHeight() - 4);

                g2.dispose();
            }
        };
        previewCard.setOpaque(false);
        previewCard.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        previewCard.setPreferredSize(new Dimension(520, 240));
        previewCard.setMaximumSize(new Dimension(520, 240));
        previewCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        previewBgPanel.add(previewCard);

        GridBagConstraints pc = new GridBagConstraints();
        pc.gridx = 0;
        pc.fill = GridBagConstraints.HORIZONTAL;

        miniTitle = new JLabel("MineSweeper", SwingConstants.CENTER);
        miniTitle.setForeground(pendingAccent);
        miniTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));

        JLabel miniSub = new JLabel("This is how buttons & titles will look", SwingConstants.CENTER);
        miniSub.setForeground(new Color(255, 255, 255, 200));
        miniSub.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        PreviewButton sampleBtn = new PreviewButton("Sample Button");

        pc.gridy = 0;
        pc.insets = new Insets(0, 0, 10, 0);
        previewCard.add(miniTitle, pc);

        pc.gridy = 1;
        pc.insets = new Insets(0, 0, 18, 0);
        previewCard.add(miniSub, pc);

        pc.gridy = 2;
        pc.insets = new Insets(0, 0, 0, 0);
        previewCard.add(sampleBtn, pc);

        rightCol.add(previewBgPanel);
        rightCol.add(Box.createVerticalStrut(12));

        return rightCol;
    }

    private void saveAccentTheme() {
        Color selected = getSelectedAccent();
        sys.setAccentColor(selected);
        UIStyles.setAccent(selected);
        titleLabel.setForeground(UIStyles.ACCENT);
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }
    private JPanel buildLeftMusicColumn() {
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Music Settings", SwingConstants.LEFT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCol.add(title);
        leftCol.add(Box.createVerticalStrut(18));

        JCheckBox musicToggle = new JCheckBox("Enable Background Music");
        musicToggle.setOpaque(false);
        musicToggle.setForeground(Color.WHITE);
        musicToggle.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        musicToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        musicToggle.setSelected(BackgroundMusic.isPlaying());

        musicToggle.addActionListener(e -> {
            if (musicToggle.isSelected())
                BackgroundMusic.start();
            else
                BackgroundMusic.stop();
        });

        leftCol.add(musicToggle);
        leftCol.add(Box.createVerticalStrut(28));

        JLabel volLabel = new JLabel("Volume");
        volLabel.setForeground(Color.WHITE);
        volLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        volLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSlider volume = new JSlider(0, 100, BackgroundMusic.getVolumePercent());
        volume.setOpaque(false);
        volume.addChangeListener(e ->
                BackgroundMusic.setVolume(volume.getValue() / 100f)
        );

        leftCol.add(volLabel);
        leftCol.add(Box.createVerticalStrut(10));
        leftCol.add(volume);

        return leftCol;
    }

    private void updatePreviewAccent(Color accent) {
        pendingAccent = accent;
        if (miniTitle != null) miniTitle.setForeground(accent);
        if (previewCard != null) previewCard.repaint();
        repaint();
    }

    private Color getSelectedAccent() {
        for (Map.Entry<String, Color> entry : ACCENTS.entrySet()) {
            String name = entry.getKey();
            Color c = entry.getValue();

            for (var e = accentGroup.getElements(); e.hasMoreElements();) {
                AbstractButton b = e.nextElement();
                if (b.isSelected() && b.getText().equals(name)) return c;
            }
        }
        return DEFAULT_GOLD;
    }

    private boolean colorsClose(Color a, Color b) {
        return Math.abs(a.getRed() - b.getRed()) < 3
                && Math.abs(a.getGreen() - b.getGreen()) < 3
                && Math.abs(a.getBlue() - b.getBlue()) < 3;
    }

    // =================================================================================
    // Async thumbnail loading
    // =================================================================================
    private void loadBackgroundThumbnailsAsync() {
        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (String key : bgThumbLabels.keySet()) {
                    ImageIcon cached = thumbCache.get(key);
                    if (cached != null) {
                        publish(new Object[]{key, cached});
                        continue;
                    }

                    String n = key.substring(2); // BG1 -> 1
                    String path = "assets/Background" + n + "/MatchBack.png";

                    Image img = loadImageAny(path);
                    if (img != null) {
                        ImageIcon icon = makeThumbIcon(img, 140, 78); 
                        thumbCache.put(key, icon);
                        publish(new Object[]{key, icon});
                    } else {
                        publish(new Object[]{key, null});
                    }
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Object[]> chunks) {
                for (Object[] chunk : chunks) {
                    String key = (String) chunk[0];
                    ImageIcon icon = (ImageIcon) chunk[1];

                    JLabel lbl = bgThumbLabels.get(key);
                    if (lbl == null) continue;

                    if (icon != null) {
                    	lbl.setText("");
                        lbl.setIcon(icon);
                        lbl.setText("");
                    } else {
                        lbl.setIcon(null);
                        lbl.setText("No Image");
                        lbl.setForeground(Color.WHITE);
                    }
                }
            }
        };
        worker.execute();
    }

    
    private ImageIcon makeThumbIcon(Image img, int targetW, int targetH) {
        // Convert to BufferedImage
        java.awt.image.BufferedImage src = new java.awt.image.BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                java.awt.image.BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = src.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        java.awt.image.BufferedImage cropped = src.getSubimage(0, 0, getWidth(), src.getHeight());

        Image scaled = cropped.getScaledInstance(targetW, targetH+100, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }


    private void loadPreviewBackgroundAsync(String key) {
        SwingWorker<Image, Void> worker = new SwingWorker<>() {
            @Override
            protected Image doInBackground() {
                String n = key.substring(2);
                String path = "assets/Background" + n + "/MatchBack.png";
                return loadImageAny(path);
            }

            @Override
            protected void done() {
                try {
                    Image img = get();
                    if (previewBgPanel != null) {
                        previewBgPanel.setBackgroundImage(img);
                    }
                } catch (Exception ignore) { }
            }
        };
        worker.execute();
    }

    // =================================================================================
    // Background key save/load helpers
    // =================================================================================
    private String safeLoadBackgroundKey() {
        try {
            return GameAssets.loadBackgroundKey();
        } catch (Throwable ignore) {
            return loadBackgroundKeyFromProperties();
        }
    }

    private void safeSaveBackgroundKey(String key) {
        try {
            GameAssets.saveBackgroundKey(key);
        } catch (Throwable ignore) {
            saveBackgroundKeyToProperties(key);
        }
    }

    private String loadBackgroundKeyFromProperties() {
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(THEME_FILE)) {
            p.load(fis);
            return p.getProperty(KEY_BG, "BG1").trim();
        } catch (Exception ignore) {
            return "BG1";
        }
    }

    private void saveBackgroundKeyToProperties(String key) {
        if (key == null || key.trim().isEmpty()) return;
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(THEME_FILE)) {
            p.load(fis);
        } catch (Exception ignore) { }

        p.setProperty(KEY_BG, key.trim());

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(THEME_FILE)) {
            p.store(fos, "Theme settings");
        } catch (Exception ex) {
            System.err.println("[THEME] Failed saving backgroundKey: " + ex.getMessage());
        }
    }

    private Image loadImageAny(String path) {
        try {
            URL cp = getClass().getClassLoader().getResource(path);
            if (cp != null) return new ImageIcon(cp).getImage();

            File f = new File(path);
            if (f.exists()) return new ImageIcon(f.getAbsolutePath()).getImage();
        } catch (Exception ignore) { }
        return null;
    }

    // =================================================================================
    // Panels / Controls
    // =================================================================================
    private static class BackgroundPanel extends JPanel {
        private final Image bg;
        BackgroundPanel(Image bg) { this.bg = bg; }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg == null) return;

            int iw = bg.getWidth(null);
            int ih = bg.getHeight(null);
            if (iw <= 0 || ih <= 0) return;

            double scale = Math.max((double) getWidth() / iw, (double) getHeight() / ih);
            int dw = (int) (iw * scale);
            int dh = (int) (ih * scale);
            g.drawImage(bg, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, this);
        }
    }

    private static class PreviewBackgroundPanel extends JPanel {
        private Image bg;
        void setBackgroundImage(Image bg) { this.bg = bg; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg == null) return;

            int iw = bg.getWidth(null);
            int ih = bg.getHeight(null);
            if (iw <= 0 || ih <= 0) return;

            double scale = Math.max((double) getWidth() / iw, (double) getHeight() / ih);
            int dw = (int) (iw * scale);
            int dh = (int) (ih * scale);
            g.drawImage(bg, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, this);
        }
    }

    private static class ButtonStyled extends JButton {
        private final Color baseFill  = new Color(20, 24, 32, 235);
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final int radius = 45;

        ButtonStyled(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 32));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Dimension d = new Dimension(320, 78);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = getModel().isRollover() ? hoverFill : baseFill;
            int w = getWidth();
            int h = getHeight();

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            g2.setStroke(new BasicStroke(4f));
            g2.setColor(UIStyles.ACCENT);
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class PreviewButton extends JButton {
        private final Color baseFill  = new Color(20, 24, 32, 235);
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final int radius = 40;

        PreviewButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 20));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(260, 64));
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color fill = getModel().isRollover() ? hoverFill : baseFill;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(pendingAccent);
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class BgChoiceButton extends JButton {

        private final Color baseFill  = new Color(20, 24, 32, 235);
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final int radius = 18;

        private boolean selectedVisual = false;

        public BgChoiceButton(String text) {
            super(text);

            setFont(new Font("Segoe UI", Font.BOLD, 20));
            setForeground(Color.WHITE);

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);

            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        }

        public void setSelectedVisual(boolean selected) {
            this.selectedVisual = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color fill = getModel().isRollover() ? hoverFill : baseFill;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(selectedVisual ? UIStyles.ACCENT : new Color(255, 255, 255, 80));
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }


    private static class ColorTileRadio extends JRadioButton {

        private final Color color;
        private final Color baseFill  = new Color(20, 24, 32, 235);
        private final Color hoverFill = new Color(40, 44, 54, 245);
        private final int radius = 18;

        private static final int DOT_D = 18;
        private static final int DOT_X = 18;
        private static final int TEXT_LEFT_PAD = 52;

        public ColorTileRadio(String name, Color color) {
            super(name);
            this.color = color;

            setOpaque(false);
            setFocusPainted(false);
            setFocusable(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setRolloverEnabled(true);

            setForeground(new Color(255, 255, 255, 230));
            setFont(new Font("Segoe UI", Font.PLAIN, 22));

            setIcon(new EmptyIcon(0, 0));
            setSelectedIcon(new EmptyIcon(0, 0));

            
            setBorder(BorderFactory.createEmptyBorder(10, TEXT_LEFT_PAD, 10, 16));

          
            Dimension d = new Dimension(0, 64);
            setPreferredSize(d);
            setMinimumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color fill = getModel().isRollover() ? hoverFill : baseFill;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w, h, radius, radius);

            g2.setStroke(new BasicStroke(3f));
            g2.setColor(isSelected()
                    ? UIStyles.ACCENT
                    : new Color(255, 255, 255, 70));
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);

            g2.dispose();
            super.paintComponent(g);

            // color dot
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cy = (h - DOT_D) / 2;
            g3.setColor(color);
            g3.fillOval(DOT_X, cy, DOT_D, DOT_D);
            g3.setColor(new Color(0, 0, 0, 140));
            g3.drawOval(DOT_X, cy, DOT_D, DOT_D);

            g3.dispose();
        }
    }


    private static class EmptyIcon implements Icon {
        private final int w, h;
        EmptyIcon(int w, int h) { this.w = w; this.h = h; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {}
        @Override public int getIconWidth() { return w; }
        @Override public int getIconHeight() { return h; }
    }
}
