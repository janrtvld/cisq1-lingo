package nl.hu.cisq1.lingo.trainer.application;

import javassist.NotFoundException;
import nl.hu.cisq1.lingo.trainer.data.SpringGameRepository;
import nl.hu.cisq1.lingo.trainer.domain.Game;
import nl.hu.cisq1.lingo.trainer.domain.exception.GameNotFoundException;
import nl.hu.cisq1.lingo.trainer.presentation.dto.ProgressDTO;
import nl.hu.cisq1.lingo.words.data.SpringWordRepository;
import nl.hu.cisq1.lingo.words.domain.Word;
import nl.hu.cisq1.lingo.words.domain.exception.WordLengthNotSupportedException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class GameService {

    private final SpringGameRepository gameRepository;
    private final SpringWordRepository wordRepository;

    public GameService(SpringGameRepository gameRepository, SpringWordRepository wordRepository) {
        this.gameRepository = gameRepository;
        this.wordRepository = wordRepository;
    }

    public ProgressDTO startGame() {
        Game game = new Game();
        Word wordToGuess = wordRepository.findRandomWordByLength(5).orElseThrow(() -> new WordLengthNotSupportedException(5));
        game.startNewRound(wordToGuess.getValue());

        this.gameRepository.save(game);

        return convertGameToProgressDTO(game);
    }

    public ProgressDTO getProgress(Long id) {
        Game game = getGameById(id);
        return convertGameToProgressDTO(game);
    }

    public ProgressDTO startNewRound(Long id) {
        Game game = getGameById(id);
        int wordLength = game.provideNextWordLength();

        Word wordToGuess = wordRepository.findRandomWordByLength(wordLength).orElseThrow(() -> new WordLengthNotSupportedException(wordLength));
        game.startNewRound(wordToGuess.getValue());

        this.gameRepository.save(game);

        return convertGameToProgressDTO(game);
    }

    public ProgressDTO guess(Long id, String attempt) {
        Game game = getGameById(id);
        game.guess(attempt);
        this.gameRepository.save(game);

        return convertGameToProgressDTO(game);
    }

    public List<ProgressDTO> getAllGames() throws NotFoundException {
        List<ProgressDTO> gamePresentationDTOS = new ArrayList<>();
        List<Game> games = this.gameRepository.findAll();

        if (games.isEmpty()) {
            throw new NotFoundException("No games found!");
        }

        for (Game game : games) {
            gamePresentationDTOS.add(convertGameToProgressDTO(game));
        }

        return gamePresentationDTOS;
    }

    private Game getGameById(Long id) {
        return this.gameRepository.findById(id).orElseThrow(() -> new GameNotFoundException(id));
    }

    private ProgressDTO convertGameToProgressDTO(Game game) {
        return new ProgressDTO.Builder(game.getId())
                .gameStatus(game.getGameStatus().getStatus())
                .score(game.getScore())
                .currentHint(game.getLatestRound().giveHint())
                .feedbackHistory(game.getLatestRound().getFeedbackHistory())
                .build();
    }

}
