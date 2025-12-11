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

    // ---------- Files ----------
    private static final Path QUESTIONS_CSV = Paths.get("questions.csv");
    private static final Path HISTORY_CSV   = Paths.get("history.csv");

    // מינימום שאלות במערכת – לא נרד מתחת לזה במחיקה
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
    // Per-level decks (אם תרצי שוב שליפה לפי level)
    private final EnumMap<QuestionLevel, ArrayDeque<Question>> decks =
            new EnumMap<>(QuestionLevel.class);

    // Deck לכל השאלות – לשימוש כששולפים שאלה רנדומלית ללא קשר לרמת התא
    private final ArrayDeque<Question> deckAll = new ArrayDeque<>();

    private void invalidateDecks() {
        decks.clear();
        deckAll.clear();
    }

    /** מספר השאלות במערכת (לבדיקות וכו'). */
    public int questionCount() {
        return questions.size();
    }

    /** Draw next question of a given level (may return null if none exist). */
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

    /**
     * Draws a random question from the whole question bank, regardless of level.
     * Uses a deck so all questions are used once before repeating.
     */
    public synchronized Question drawRandomQuestion() {
        if (questions.isEmpty()) return null;
        if (deckAll.isEmpty()) {
            List<Question> pool = new ArrayList<>(questions);
            Collections.shuffle(pool, ThreadLocalRandom.current());
            deckAll.addAll(pool);
        }
        return deckAll.pollFirst();
    }

    // ---------- Public mutators ----------
    public synchronized void addRecord(GameRecord r) {
        history.add(r);
        appendHistoryCsv(r);
    }

    /** Add question + שמירה ל־CSV, בלי הגבלה על מקסימום. */
    public synchronized void addQuestion(Question q) {
        questions.add(q);
        saveAllQuestions();
        invalidateDecks();
    }

    /**
     * Deletes a question by ID.
     * - Deletion is blocked if the system must keep at least MIN_QUESTIONS.
     * - After a successful delete, ALL question IDs are renumbered sequentially (1..N)
     *   so there are no gaps in the numbering.
     */
    public synchronized boolean deleteQuestion(String id) {

        // Prevent deletion if we must keep at least MIN_QUESTIONS in the system
        if (questions.size() <= MIN_QUESTIONS)
            return false;

        // Try deleting the question with the matching ID
        boolean removed = questions.removeIf(q -> q.id().equals(id));

        if (removed) {

            // --- Renumber all remaining questions to IDs 1..N ---
            List<Question> renumbered = new ArrayList<>(questions.size());

            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String newId = String.valueOf(i + 1);  // IDs become 1,2,3,...

                // Create a new Question object with the SAME content but a NEW ID
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

            // Replace the old list with the newly renumbered list
            questions.clear();
            questions.addAll(renumbered);

            // Save updated CSV and reset the question decks
            saveAllQuestions();
            invalidateDecks();
        }

        return removed;
    }



    /**
     * Returns the next numeric question id (max existing + 1).
     * אם יש ID לא מספרי – מתעלמים ממנו לצורך חישוב ה־max.
     */
    public synchronized int nextQuestionId() {
        int max = 0;
        for (Question q : questions) {
            try {
                int v = Integer.parseInt(q.id().trim());
                if (v > max) max = v;
            } catch (NumberFormatException ignored) { }
        }
        return max + 1;
    }

    // ---------- CSV helpers for questions (comma separated) ----------
    private static String esc(String s) {
        if (s == null) return "";
        boolean q = s.contains(",") || s.contains("\"")
                    || s.contains("\n") || s.contains("\r");
        String body = s.replace("\"", "\"\"");
        return q ? "\"" + body + "\"" : body;
    }

    /** CSV splitter for comma-separated values */
    private static String[] splitCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQ) {
                if (ch == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
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
    //                QUESTIONS CSV FORMAT
    //
    // ID,Question,Difficulty,A,B,C,D,CorrectAnswer
    //
    // Difficulty: 1=EASY, 2=MEDIUM, 3=HARD, 4=MASTER
    // CorrectAnswer: A/B/C/D
    // ============================================================

    private void loadQuestions() {
        questions.clear();
        if (!Files.exists(QUESTIONS_CSV)) return;

        try (BufferedReader br =
                     Files.newBufferedReader(QUESTIONS_CSV, StandardCharsets.UTF_8)) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                // skip header row
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

                // map difficulty 1..4 → QuestionLevel enum
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

                // map A/B/C/D → 0..3  (anything else is clamped to 0)
                int numericIndex;
                if (correctLetter.length() == 1) {
                    numericIndex = correctLetter.charAt(0) - 'A';
                } else {
                    // defensive: if someone puts "65" or weird stuff, treat as A
                    numericIndex = 0;
                }
                int correctIndex = Math.max(0, Math.min(3, numericIndex));

                List<String> opts = List.of(optA, optB, optC, optD);

                questions.add(new Question(id, text, opts, correctIndex, level));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        invalidateDecks();
    }

    private void saveAllQuestions() {
        try (BufferedWriter bw =
                     Files.newBufferedWriter(QUESTIONS_CSV, StandardCharsets.UTF_8,
                             StandardOpenOption.CREATE,
                             StandardOpenOption.TRUNCATE_EXISTING)) {

            // header
            bw.write("ID,Question,Difficulty,A,B,C,D,CorrectAnswer");
            bw.newLine();

            for (Question q : questions) {
                int diffNum = switch (q.level()) {
                    case EASY   -> 1;
                    case MEDIUM -> 2;
                    case HARD   -> 3;
                    case MASTER -> 4;
                };

                char correct = (char) ('A' + q.correctIndex()); // 0→A, 1→B, ...

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

    // ---------- HISTORY (unchanged structure, ; separated) ----------
    private static List<String> parseLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQ) {
                if (ch == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); i++;
                } else if (ch == '"') {
                    inQ = false;
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == ';') {
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
        return out;
    }

    private void loadHistory() {
        history.clear();
        if (!Files.exists(HISTORY_CSV)) return;
        try (BufferedReader br =
                     Files.newBufferedReader(HISTORY_CSV, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> f = parseLine(line);
                if (f.size() < 8) continue;
                GameRecord r = new GameRecord(
                        f.get(0), f.get(1), DifficultyLevel.valueOf(f.get(2)),
                        Integer.parseInt(f.get(3)), Integer.parseInt(f.get(4)),
                        Boolean.parseBoolean(f.get(5)),
                        Long.parseLong(f.get(6)),
                        Long.parseLong(f.get(7)));
                history.add(r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendHistoryCsv(GameRecord r) {
        try (BufferedWriter bw =
                     Files.newBufferedWriter(HISTORY_CSV, StandardCharsets.UTF_8,
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
    
 // inside SysData class
    public synchronized void replaceQuestion(String oldId, Question updated) {
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).id().equals(oldId)) {
                // replace in place – does NOT change list size
                questions.set(i, updated);
                saveAllQuestions();
                invalidateDecks();
                return;
            }
        }
        // If not found for some reason – just add as new
        questions.add(updated);
        saveAllQuestions();
        invalidateDecks();
    }

}
