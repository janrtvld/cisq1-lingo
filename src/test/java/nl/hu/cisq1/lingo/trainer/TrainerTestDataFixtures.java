package nl.hu.cisq1.lingo.trainer;

import nl.hu.cisq1.lingo.trainer.data.SpringGameRepository;
import nl.hu.cisq1.lingo.trainer.domain.Game;
import nl.hu.cisq1.lingo.words.data.SpringWordRepository;
import nl.hu.cisq1.lingo.words.domain.Word;
import org.springframework.boot.CommandLineRunner;

public class TrainerTestDataFixtures implements CommandLineRunner {
    private final SpringGameRepository gameRepository;

    public TrainerTestDataFixtures(SpringGameRepository gameRepository ) {
        this.gameRepository = gameRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Game gameWithNoRounds = new Game();

        Game gameWithPlayingRound = new Game();
        gameWithPlayingRound.startNewRound("PLANK");

        Game gameWithEliminatedPlayer = new Game();
        gameWithEliminatedPlayer.startNewRound("PLANK");
        gameWithEliminatedPlayer.guess("LOSER");
        gameWithEliminatedPlayer.guess("LOSER");
        gameWithEliminatedPlayer.guess("LOSER");
        gameWithEliminatedPlayer.guess("LOSER");
        gameWithEliminatedPlayer.guess("LOSER");

        this.gameRepository.save(gameWithNoRounds);
        this.gameRepository.save(gameWithPlayingRound);
        this.gameRepository.save(gameWithEliminatedPlayer);
    }
}
