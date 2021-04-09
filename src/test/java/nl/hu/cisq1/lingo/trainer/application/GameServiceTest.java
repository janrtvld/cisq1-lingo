package nl.hu.cisq1.lingo.trainer.application;

import javassist.NotFoundException;
import nl.hu.cisq1.lingo.trainer.data.SpringGameRepository;
import nl.hu.cisq1.lingo.trainer.domain.Game;
import nl.hu.cisq1.lingo.trainer.domain.exception.GameNotFoundException;
import nl.hu.cisq1.lingo.trainer.application.dto.ProgressDTO;
import nl.hu.cisq1.lingo.words.data.SpringWordRepository;
import nl.hu.cisq1.lingo.words.domain.Word;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * This is a unit test.
 *
 * It tests the behaviors of our system under test,
 * GameService, in complete isolation:
 * - its methods are called by the test framework instead of a controller
 * - the GameService calls a test double instead of an actual repository
 */
@DisplayName("GameService")
class GameServiceTest {

    private SpringGameRepository gameRepository;
    private SpringWordRepository wordRepository;
    private GameService service;
    private Game game;

    @BeforeEach
    @DisplayName("initiates mocks and service for tests")
    void beforeEach() {
        gameRepository = mock(SpringGameRepository.class);
        wordRepository = mock(SpringWordRepository.class);
        this.game = new Game();

        when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.of(game));
        when(wordRepository.findRandomWordByLength(anyInt()))
                .thenReturn(Optional.of(new Word("BLOEM")));

        service = new GameService(gameRepository,wordRepository);
    }

    @Test
    @DisplayName("Starting a game returns progress of the game")
    void startGameReturnsNewGame() {
        game.startNewRound("BLOEM");
        ProgressDTO result = service.startGame();
        ProgressDTO expected = convertGameToProgressDTO(game);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Get progress throws exception if game not found")
    void getProgressReturnsExceptionIfGameNotFound() {
        when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.getProgress(0L));
    }

    @Test
    @DisplayName("Get progress returns game as progress DTO")
    void getProgressReturnsProgressDTO() {
        game.startNewRound("BLOEM");
        ProgressDTO expected = convertGameToProgressDTO(game);

        ProgressDTO result = service.getProgress(anyLong());

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("New round throws exception if game does not exists")
    void newRoundThrowsErrorByNonExistingGame() {
        when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.startNewRound(0L));
    }

    @Test
    @DisplayName("New round returns the new progress with the created round")
    void newRoundReturnsGameProgress() {
        game.startNewRound("BLOEM");
        game.guess("BLOEM");

        ProgressDTO result = service.startNewRound(anyLong());

        ProgressDTO expected = convertGameToProgressDTO(game);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Guess throws exception if game does not exists")
    void guessThrowsExceptionByNonExistingGame() {
        when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.guess(0L,"LOSER"));
    }

    @Test
    @DisplayName("Guess returns the new progress with the processed guess")
    void guessIsReturnedInGameProgress() {
        game.startNewRound("BLOEM");
        ProgressDTO result = service.guess(anyLong(),"BLOEI");

        game.guess("BLOEI");
        ProgressDTO expected = convertGameToProgressDTO(game);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("throw exception by no games found")
    void allGamesEmptyException() {
        List<Game> gameList = new ArrayList<>();
        when(gameRepository.findAll())
                .thenReturn(gameList);

        assertThrows(NotFoundException.class, () -> service.getAllGames());
    }

    @Test
    @DisplayName("return a list of games")
    void allGamesReturnsListOfGames() throws NotFoundException {
        game.startNewRound("GRAAL");

        List<Game> gameList = List.of(game);

        when(gameRepository.findAll())
                .thenReturn(gameList);

        assertEquals(1, service.getAllGames().size());
    }

    private static ProgressDTO convertGameToProgressDTO(Game game) {
        return new ProgressDTO.Builder(game.getId())
                .gameStatus(game.getGameStatus().getStatus())
                .score(game.getScore())
                .currentHint(game.getLatestRound().giveHint())
                .feedbackHistory(game.getLatestRound().getFeedbackHistory())
                .build();
    }
}