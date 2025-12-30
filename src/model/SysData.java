package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SysData {

    // ---------- Singleton ----------
    private static SysData INSTANCE;
    public static synchronized SysData getInstance() {
        if (INSTANCE == null) INSTANCE = new SysData();
        return INSTANCE;
    }

    private SysData() {
        loadQuestions();
        loadHistory();
    }

    // ============================================================
    //         DYNAMIC CSV PATHS
    // ============================================================

    private static Path questionsPath() {
        try {
            Path jarDir = Paths.get(
                    SysData.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();

            return jarDir.resolve("questions.csv");

        } catch (Exception e) {
            return Paths.get("questions.csv");
        }
    }

    private static Path historyPath() {
        try {
            Path jarDir = Paths.get(
                    SysData.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();

            return jarDir.resolve("history.csv");

        } catch (Exception e) {
            return Paths.get("history.csv");
        }
    }

    // ---------- Constants ----------
    private static final int MIN_QUESTIONS = 20;

    // ---------- GameRecord ----------
    public static class GameRecord {
        public final String p1, p2;
        public final DifficultyLevel level;
        public final int hearts, points;
        public final boolean won;
        public final long timeSec;
        public final long timestamp;

        public GameRecord(String p1, String p2, DifficultyLevel lvl,
                          int hearts, int points, boolean won, long timeSec) {
            this(p1, p2, lvl, hearts, points, won, timeSec,
                 System.currentTimeMillis());
        }

        public GameRecord(String p1, String p2, DifficultyLevel lvl,
                          int hearts, int points, boolean won,
                          long timeSec, long ts) {
            this.p1 = p1;
            this.p2 = p2;
            this.level = lvl;
            this.hearts = hearts;
            this.points = points;
            this.won = won;
            this.timeSec = timeSec;
            this.timestamp = ts;
        }
    }

    // ---------- Data ----------
    private final List<GameRecord> history   = new ArrayList<>();
    private final List<Question>   questions = new ArrayList<>();

    public List<GameRecord> history()   { return Collections.unmodifiableList(history); }
    public List<Question>   questions() { return Collections.unmodifiableList(questions); }

    // ---------- Question decks ----------
    private final EnumMap<QuestionLevel, ArrayDeque<Question>> decks =
            new EnumMap<>(QuestionLevel.class);

    private final ArrayDeque<Question> deckAll = new ArrayDeque<>();

    private void invalidateDecks() {
        decks.clear();
        deckAll.clear();
    }

    public int questionCount() {
        return questions.size();
    }

    public synchronized Question drawQuestion(QuestionLevel lvl) {
        ArrayDeque<Question> deck = decks.computeIfAbsent(lvl, k -> new ArrayDeque<>());
        if (deck.isEmpty()) {
            List<Question> pool = new ArrayList<>();
            for (Question q : questions) {
                if (q.level() == lvl) pool.add(q);
            }
            Collections.shuffle(pool, ThreadLocalRandom.current());
            deck.addAll(pool);
        }
        return deck.isEmpty() ? null : deck.pollFirst();
    }

    public synchronized Question drawRandomQuestion() {
        if (questions.isEmpty()) return null;
        if (deckAll.isEmpty()) {
            List<Question> pool = new ArrayList<>(questions);
            Collections.shuffle(pool, ThreadLocalRandom.current());
            deckAll.addAll(pool);
        }
        return deckAll.pollFirst();
    }

    // ============================================================
    // Public mutators
    // ============================================================

    public synchronized void addRecord(GameRecord r) {
        history.add(r);
        appendHistoryCsv(r);
    }

    public synchronized void addQuestion(Question q) {
        questions.add(q);
        saveAllQuestions();
        invalidateDecks();
    }

    public synchronized boolean deleteQuestion(String id) {
        if (questions.size() <= MIN_QUESTIONS)
            return false;

        boolean removed = questions.removeIf(q -> q.id().equals(id));

        if (removed) {
            List<Question> renumbered = new ArrayList<>();

            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String newId = String.valueOf(i + 1);

                renumbered.add(
                        new Question(
                                newId,
                                q.text(),
                                q.options(),
                                q.correctIndex(),
                                q.level()
                        )
                );
            }

            questions.clear();
            questions.addAll(renumbered);

            saveAllQuestions();
            invalidateDecks();
        }

        return removed;
    }

    public synchronized int nextQuestionId() {
        int max = 0;
        for (Question q : questions) {
            try {
                int v = Integer.parseInt(q.id().trim());
                if (v > max) max = v;
            } catch (Exception ignored) {}
        }
        return max + 1;
    }

    // ============================================================
    // CSV helpers
    // ============================================================

    private static String esc(String s) {
        if (s == null) return "";
        boolean q = s.contains(",") || s.contains("\"") ||
                s.contains("\n") || s.contains("\r");
        String body = s.replace("\"", "\"\"");
        return q ? "\"" + body + "\"" : body;
    }

    private static String[] splitCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQ) {
                if (ch == '"' && i+1 < line.length() && line.charAt(i+1)=='"') {
                    cur.append('"'); i++;
                } else if (ch == '"') {
                    inQ = false;
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else if (ch == '"') {
                    inQ = true;
                } else {
                    cur.append(ch);
                }
            }
        }

        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    // ============================================================
    // Load Questions
    // ============================================================

    private void loadQuestions() {
        questions.clear();
        Path csv = questionsPath();

        if (!Files.exists(csv)) return;

        try (BufferedReader br =
                     Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                if (first) {
                    first = false;
                    String low = line.toLowerCase();
                    if (low.contains("question") && low.contains("difficulty"))
                        continue;
                }

                String[] f = splitCsvLine(line);
                if (f.length < 8) continue;

                String id   = f[0].trim();
                String text = f[1].trim();
                String diff = f[2].trim();

                String optA = f[3].trim();
                String optB = f[4].trim();
                String optC = f[5].trim();
                String optD = f[6].trim();

                String correctLetter = f[7].trim().toUpperCase(Locale.ROOT);

                QuestionLevel level;
                try {
                    int d = Integer.parseInt(diff);
                    level = switch (d) {
                        case 1 -> QuestionLevel.EASY;
                        case 2 -> QuestionLevel.MEDIUM;
                        case 3 -> QuestionLevel.HARD;
                        case 4 -> QuestionLevel.MASTER;
                        default -> QuestionLevel.EASY;
                    };
                } catch (Exception e) {
                    level = QuestionLevel.EASY;
                }

                int numericIndex;
                if (correctLetter.length() == 1)
                    numericIndex = correctLetter.charAt(0) - 'A';
                else
                    numericIndex = 0;

                int correctIndex = Math.max(0, Math.min(3, numericIndex));

                List<String> opts = List.of(optA, optB, optC, optD);

                questions.add(new Question(id, text, opts, correctIndex, level));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        invalidateDecks();
    }

    // ============================================================
    // Save Questions
    // ============================================================

    private void saveAllQuestions() {
        try (BufferedWriter bw =
                     Files.newBufferedWriter(questionsPath(), StandardCharsets.UTF_8,
                             StandardOpenOption.CREATE,
                             StandardOpenOption.TRUNCATE_EXISTING)) {

            bw.write("ID,Question,Difficulty,A,B,C,D,CorrectAnswer");
            bw.newLine();

            for (Question q : questions) {
                int diffNum = switch (q.level()) {
                    case EASY   -> 1;
                    case MEDIUM -> 2;
                    case HARD   -> 3;
                    case MASTER -> 4;
                };

                char correct = (char)('A' + q.correctIndex());

                String line = String.join(",",
                        esc(q.id()),
                        esc(q.text()),
                        String.valueOf(diffNum),
                        esc(q.options().get(0)),
                        esc(q.options().get(1)),
                        esc(q.options().get(2)),
                        esc(q.options().get(3)),
                        String.valueOf(correct)
                );

                bw.write(line);
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // HISTORY LOAD
    // ============================================================

    private static List<String> parseLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQ) {
                if (ch=='"' && i+1<line.length() && line.charAt(i+1)=='"') {
                    cur.append('"'); i++;
                } else if (ch=='"') {
                    inQ = false;
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch==';') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else if (ch=='"') {
                    inQ = true;
                } else {
                    cur.append(ch);
                }
            }
        }

        out.add(cur.toString());
        return out;
    }

    private void loadHistory() {
        history.clear();
        Path csv = historyPath();

        if (!Files.exists(csv)) return;

        try (BufferedReader br =
                     Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                List<String> f = parseLine(line);
                if (f.size() < 8) continue;

                GameRecord r = new GameRecord(
                        f.get(0), f.get(1),
                        DifficultyLevel.valueOf(f.get(2)),
                        Integer.parseInt(f.get(3)),
                        Integer.parseInt(f.get(4)),
                        Boolean.parseBoolean(f.get(5)),
                        Long.parseLong(f.get(6)),
                        Long.parseLong(f.get(7))
                );

                history.add(r);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // HISTORY SAVE
    // ============================================================

    private void appendHistoryCsv(GameRecord r) {
        try (BufferedWriter bw =
                     Files.newBufferedWriter(historyPath(), StandardCharsets.UTF_8,
                             StandardOpenOption.CREATE,
                             StandardOpenOption.APPEND)) {

            String line = String.join(";",
                    esc(r.p1), esc(r.p2), r.level.name(),
                    String.valueOf(r.hearts), String.valueOf(r.points),
                    String.valueOf(r.won), String.valueOf(r.timeSec),
                    String.valueOf(r.timestamp));

            bw.write(line);
            bw.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // REPLACE QUESTION
    // ============================================================

    public synchronized void replaceQuestion(String oldId, Question updated) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).id().equals(oldId)) {
                questions.set(i, updated);
                saveAllQuestions();
                invalidateDecks();
                return;
            }
        }

        questions.add(updated);
        saveAllQuestions();
        invalidateDecks();
    }
}
