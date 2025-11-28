package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Singleton data store for:
 * - Questions bank 
 * - Game history
 */
public class SysData {

    /* Singleton instance field */
    private static SysData INSTANCE;

    /* Singleton accessor */
    public static synchronized SysData getInstance() {
        if (INSTANCE == null) INSTANCE = new SysData();
        return INSTANCE;
    }

    /* Private ctor loads CSV files */
    private SysData() { loadQuestions(); }

    /* Questions CSV file (new comma format) */
    private static Path questionsPath() {
        try {
            // folder that contains the running jar (or class files in IDE)
            Path jarDir = Paths.get(
                    SysData.class.getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();

            return jarDir.resolve("questions.csv");

        } catch (Exception e) {
            // fallback: current working directory
            return Paths.get("questions.csv");
        }
    }


    /* Minimum number of questions allowed in system */
    private static final int MIN_QUESTIONS = 20;

    /* List of all questions */
    private final List<Question>   questions = new ArrayList<>();

    /* Read-only questions getter */
    public List<Question>   questions() { return Collections.unmodifiableList(questions); }
    
    // ---------- Question decks ----------
    // Per-level decks
    private final EnumMap<QuestionLevel, ArrayDeque<Question>> decks =
            new EnumMap<>(QuestionLevel.class);

    /* Deck for random questions regardless of level */
    private final ArrayDeque<Question> deckAll = new ArrayDeque<>();

    /* Clears all decks after data changes */
    private void invalidateDecks(){
        decks.clear();
        deckAll.clear();
    }

    /* Draw random question from all levels (use-all-before-repeat) */
    public synchronized Question drawRandomQuestion(){
        if (questions.isEmpty()) return null;
        if (deckAll.isEmpty()){
            List<Question> pool = new ArrayList<>(questions);
            Collections.shuffle(pool, ThreadLocalRandom.current());
            deckAll.addAll(pool);
        }
        return deckAll.pollFirst();
    }

    /* Add question + save all + reset decks */
    public synchronized void addQuestion(Question q){
        questions.add(q);
        saveAllQuestions();
        invalidateDecks();
    }

    /* Delete question with MIN_QUESTIONS protection */
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

    /*  Returns max numeric id + 1 for auto-IDs */
    public synchronized int nextQuestionId(){
        int max = 0;
        for (Question q : questions){
            try {
                int v = Integer.parseInt(q.id().trim());
                if (v > max) max = v;
            } catch (NumberFormatException ignored) { }
        }
        return max + 1;
    }


    /* Escapes field for comma CSV */
    private static String esc(String s){
        if(s==null) return "";
        boolean q = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String body = s.replace("\"","\"\"");
        return q ? "\""+body+"\"" : body;
    }

    /* Splits one comma-CSV line while honoring quotes */
    private static String[] splitCsvLine(String line){
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;

        for (int i=0;i<line.length();i++){
            char ch = line.charAt(i);
            if (inQ){
                if (ch=='"' && i+1<line.length() && line.charAt(i+1)=='"'){ cur.append('"'); i++; }
                else if (ch=='"'){ inQ=false; }
                else cur.append(ch);
            } else {
                if (ch==','){ out.add(cur.toString()); cur.setLength(0); }
                else if (ch=='"'){ inQ=true; }
                else cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    /* Load questions from CSV */
    private void loadQuestions(){

        questions.clear();
        Path QUESTIONS_CSV = questionsPath();
        if(!Files.exists(QUESTIONS_CSV)) return;

        try(BufferedReader br = Files.newBufferedReader(QUESTIONS_CSV, StandardCharsets.UTF_8)){
            String line;
            boolean first = true;

            while((line=br.readLine())!=null){
                if(line.isBlank()) continue;

                if(first){
                    first=false;
                    String low = line.toLowerCase();
                    if(low.contains("question") && low.contains("difficulty"))
                        continue;
                }

                String[] f = splitCsvLine(line);
                if(f.length < 8) continue;

                String id    = f[0].trim();
                String text  = f[1].trim();
                String diff  = f[2].trim();

                String optA  = f[3].trim();
                String optB  = f[4].trim();
                String optC  = f[5].trim();
                String optD  = f[6].trim();

                String correctLetter = f[7].trim().toUpperCase();

                QuestionLevel level;
                try{
                    int d = Integer.parseInt(diff);
                    level = switch(d){
                        case 1 -> QuestionLevel.EASY;
                        case 2 -> QuestionLevel.MEDIUM;
                        case 3 -> QuestionLevel.HARD;
                        case 4 -> QuestionLevel.MASTER;
                        default -> QuestionLevel.EASY;
                    };
                } catch(Exception e){
                    level = QuestionLevel.EASY;
                }

                int correctIndex = Math.max(0, Math.min(3, correctLetter.charAt(0) - 'A'));
                List<String> opts = List.of(optA,optB,optC,optD);

                questions.add(new Question(id, text, opts, correctIndex, level));
            }

        } catch(IOException e){
            e.printStackTrace();
        }

        invalidateDecks();
    }

    /* Save all questions to CSV (overwrite) */
    private void saveAllQuestions(){
    	Path QUESTIONS_CSV = questionsPath();
        try(BufferedWriter bw = Files.newBufferedWriter(QUESTIONS_CSV, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)){

            bw.write("ID,Question,Difficulty,A,B,C,D,CorrectAnswer");
            bw.newLine();

            for(Question q: questions){
                int diffNum = switch(q.level()){
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

        } catch(IOException e){
            e.printStackTrace();
        }
    }
    
    // ---------- GameRecord ----------
    public static class GameRecord {
        public final String p1,p2;
        public final DifficultyLevel level;
        public final int hearts, points;
        public final boolean won;
        public final long timeSec;
        public final long timestamp;
        public GameRecord(String p1,String p2,DifficultyLevel lvl,int hearts,int points,boolean won,long timeSec){
            this(p1,p2,lvl,hearts,points,won,timeSec,System.currentTimeMillis());
        }
        public GameRecord(String p1,String p2,DifficultyLevel lvl,int hearts,int points,boolean won,long timeSec,long ts){
            this.p1=p1; this.p2=p2; this.level=lvl; this.hearts=hearts; this.points=points; this.won=won; this.timeSec=timeSec; this.timestamp=ts;
        }
    }
}