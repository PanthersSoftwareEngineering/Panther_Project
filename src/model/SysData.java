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

    // =============================================================
    //              *** CSV PATH RESOLUTION LIKE FIRST CODE ***
    // =============================================================

    private static Path baseFolder() {
        try {
            Path jarDir = Paths.get(
                    SysData.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();

            return jarDir; // folder of jar or /bin in Eclipse

        } catch (Exception e) {
            return Paths.get("."); // fallback: working directory
        }
    }

    private static Path questionsPath() {
        return baseFolder().resolve("questions.csv");
    }

    private static Path historyPath() {
        return baseFolder().resolve("history.csv");
    }

    // מינימום שאלות במערכת
    private static final int MIN_QUESTIONS = 20;

    // ---------- GameRecord ----------
    public static class GameRecord {
        public final String p1,p2;
        public final DifficultyLevel level;
        public final int hearts, points;
        public final boolean won;
        public final long timeSec;
        public final long timestamp;

        public GameRecord(String p1,String p2,DifficultyLevel lvl,int hearts,int points,
                          boolean won,long timeSec) {
            this(p1,p2,lvl,hearts,points,won,timeSec,System.currentTimeMillis());
        }

        public GameRecord(String p1,String p2,DifficultyLevel lvl,int hearts,int points,
                          boolean won,long timeSec,long ts){
            this.p1=p1; this.p2=p2; this.level=lvl;
            this.hearts=hearts; this.points=points;
            this.won=won; this.timeSec=timeSec;
            this.timestamp=ts;
        }
    }

    // ---------- Data ----------
    private final List<GameRecord> history   = new ArrayList<>();
    private final List<Question>   questions = new ArrayList<>();

    public List<GameRecord> history()   { return Collections.unmodifiableList(history); }
    public List<Question>   questions() { return Collections.unmodifiableList(questions); }

    // ---------- Decks ----------
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

    public synchronized Question drawRandomQuestion() {
        if (questions.isEmpty()) return null;

        if (deckAll.isEmpty()){
            List<Question> pool = new ArrayList<>(questions);
            Collections.shuffle(pool, ThreadLocalRandom.current());
            deckAll.addAll(pool);
        }
        return deckAll.pollFirst();
    }

    // ---------- Mutators ----------
    public synchronized void addRecord(GameRecord r) {
        history.add(r);
        appendHistoryCsv(r);
    }

    public synchronized void addQuestion(Question q) {
        questions.add(q);
        saveAllQuestions();
        invalidateDecks();
    }

    public synchronized boolean deleteQuestion(String id){
        if (questions.size() <= MIN_QUESTIONS)
            return false;

        boolean ok = questions.removeIf(q -> q.id().equals(id));
        if (ok){
            saveAllQuestions();
            invalidateDecks();
        }
        return ok;
    }

    public synchronized int nextQuestionId(){
        int max = 0;
        for (Question q : questions){
            try {
                int v = Integer.parseInt(q.id().trim());
                if (v > max) max = v;
            } catch(NumberFormatException ignored){}
        }
        return max + 1;
    }

    // ---------- CSV Helpers ----------
    private static String esc(String s){
        if(s==null) return "";
        boolean q = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String body = s.replace("\"","\"\"");
        return q ? "\"" + body + "\"" : body;
    }

    private static String[] splitCsvLine(String line){
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;

        for (int i=0;i<line.length();i++){
            char ch = line.charAt(i);
            if (inQ){
                if(ch=='"' && i+1<line.length() && line.charAt(i+1)=='"'){ cur.append('"'); i++; }
                else if(ch=='"'){ inQ=false; }
                else cur.append(ch);
            } else {
                if(ch==','){ out.add(cur.toString()); cur.setLength(0); }
                else if(ch=='"'){ inQ=true; }
                else cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    // =============================================================
    //                        LOAD QUESTIONS
    // =============================================================
    private void loadQuestions(){
        questions.clear();

        Path csv = questionsPath();
        if(!Files.exists(csv)) return;

        try(BufferedReader br = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;

            while((line = br.readLine()) != null){
                if (line.isBlank()) continue;

                if(first){
                    first=false;
                    String low = line.toLowerCase();
                    if(low.contains("question") && low.contains("difficulty"))
                        continue;
                }

                String[] f = splitCsvLine(line);
                if(f.length < 8) continue;

                String id   = f[0].trim();
                String text = f[1].trim();
                int    diff = Integer.parseInt(f[2].trim());

                QuestionLevel level = switch(diff){
                    case 1 -> QuestionLevel.EASY;
                    case 2 -> QuestionLevel.MEDIUM;
                    case 3 -> QuestionLevel.HARD;
                    case 4 -> QuestionLevel.MASTER;
                    default -> QuestionLevel.EASY;
                };

                List<String> opts = List.of(f[3].trim(), f[4].trim(), f[5].trim(), f[6].trim());
                int correctIndex = f[7].trim().charAt(0) - 'A';

                questions.add(new Question(id, text, opts, correctIndex, level));
            }

        } catch(IOException e){
            e.printStackTrace();
        }

        invalidateDecks();
    }

    private void saveAllQuestions() {
        Path csv = questionsPath();

        try(BufferedWriter bw = Files.newBufferedWriter(csv, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            bw.write("ID,Question,Difficulty,A,B,C,D,CorrectAnswer");
            bw.newLine();

            for (Question q : questions){
                int diffNum = switch(q.level()){
                    case EASY -> 1;
                    case MEDIUM -> 2;
                    case HARD -> 3;
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

        } catch(IOException e){
            e.printStackTrace();
        }
    }

    // =============================================================
    //                        HISTORY
    // =============================================================
    private List<String> parseLine(String line){
        List<String> out=new ArrayList<>();
        StringBuilder cur=new StringBuilder();
        boolean inQ=false;

        for(int i=0;i<line.length();i++){
            char ch=line.charAt(i);
            if(inQ){
                if(ch=='"' && i+1<line.length() && line.charAt(i+1)=='"'){ cur.append('"'); i++; }
                else if(ch=='"'){ inQ=false; }
                else cur.append(ch);
            } else {
                if(ch==';'){ out.add(cur.toString()); cur.setLength(0); }
                else if(ch=='"'){ inQ=true; }
                else cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out;
    }

    private void loadHistory(){
        history.clear();
        Path csv = historyPath();

        if(!Files.exists(csv)) return;

        try(BufferedReader br = Files.newBufferedReader(csv, StandardCharsets.UTF_8)){
            String line;

            while((line = br.readLine()) != null){
                if(line.isBlank()) continue;

                List<String> f = parseLine(line);
                if(f.size() < 8) continue;

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

        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void appendHistoryCsv(GameRecord r){
        Path csv = historyPath();

        try(BufferedWriter bw = Files.newBufferedWriter(csv, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            String line = String.join(";",
                    esc(r.p1),
                    esc(r.p2),
                    r.level.name(),
                    String.valueOf(r.hearts),
                    String.valueOf(r.points),
                    String.valueOf(r.won),
                    String.valueOf(r.timeSec),
                    String.valueOf(r.timestamp)
            );

            bw.write(line);
            bw.newLine();

        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
