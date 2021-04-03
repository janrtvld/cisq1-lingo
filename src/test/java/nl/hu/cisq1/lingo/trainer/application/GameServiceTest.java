package nl.hu.cisq1.lingo.trainer.application;

import javassist.NotFoundException;
import nl.hu.cisq1.lingo.trainer.data.SpringGameRepository;
import nl.hu.cisq1.lingo.trainer.domain.Game;
import nl.hu.cisq1.lingo.trainer.domain.exception.GameNotFoundException;
import nl.hu.cisq1.lingo.trainer.presentation.dto.ProgressPresentationDTO;
import nl.hu.cisq1.lingo.words.data.SpringWordRepository;
import nl.hu.cisq1.lingo.words.domain.Word;
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

/**
 * This is a unit test.
 *
 * It tests the behaviors of our system under test,
 * WordService, in complete isolation:
 * - its methods are called by the test framework instead of a controller
 * - the WordService calls a test double instead of an actual repository
 */
class GameServiceTest {

    private SpringGameRepository gameRepository;
    private SpringWordRepository wordRepository;
    private GameService service;

    @BeforeEach
    @DisplayName("initiate mocks and service for tests")
    void init() {
        gameRepository = Mockito.mock(SpringGameRepository.class);
        wordRepository = Mockito.mock(SpringWordRepository.class);
        service = new GameService(gameRepository,wordRepository);
    }

    @Test
    @DisplayName("Starting a game returns progress of the game")
    void startGameReturnsNewGame() {
        Game game = new Game();
        game.startNewRound("tower");

        Mockito.when(gameRepository.save(game))
                .thenReturn(game);
        Mockito.when(wordRepository.findRandomWordByLength(anyInt()))
                .thenReturn(Optional.of(new Word("tower")));

        ProgressPresentationDTO result = service.startGame();
        ProgressPresentationDTO expected = convertGameToProgressDTO(game);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Get progress throws exception if game not found")
    void getProgressReturnsExceptionIfGameNotFound() {
        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.getProgress(0L));
    }

    @Test
    @DisplayName("Get progress returns game as progress DTO")
    void getProgressReturnsProgressDTO() {
        Game game = new Game();
        game.startNewRound("tower");

        ProgressPresentationDTO expected = convertGameToProgressDTO(game);

        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.of(game));

        ProgressPresentationDTO result = service.getProgress(anyLong());

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("New round throws exception if game does not exists")
    void newRoundThrowsErrorByNonExistingGame() {
        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.startNewRound(0L));
    }

    @Test
    @DisplayName("New round returns the new progress with the created round")
    void newRoundReturnsGameProgress() {
        Game game = new Game();

        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Game()));
        Mockito.when(wordRepository.findRandomWordByLength(anyInt()))
                .thenReturn(Optional.of(new Word("tower")));

        ProgressPresentationDTO result = service.startNewRound(anyLong());

        game.startNewRound("tower");
        ProgressPresentationDTO expected = convertGameToProgressDTO(game);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Guess throws exception if game does not exists")
    void guessThrowsExceptionByNonExistingGame() {
        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.guess(0L,"LOSER"));
    }

    @Test
    @DisplayName("Guess returns the new progress with the processed guess")
    void guessIsReturnedInGameProgress() {
        Game game = new Game();
        game.startNewRound("tower");

        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.of(game));

        ProgressPresentationDTO result = service.guess(anyLong(),"lower");

        game.guess("lower");
        ProgressPresentationDTO expected = convertGameToProgressDTO(game);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("throw exception by no games found")
    void allGamesEmptyException() {
        List<Game> gameList = new ArrayList<>();
        Mockito.when(gameRepository.findAll())
                .thenReturn(gameList);

        assertThrows(NotFoundException.class, () -> service.getAllGames());
    }

    @Test
    @DisplayName("return a list of games")
    void allGamesReturnsListOfGames() throws NotFoundException {
        Game game = new Game();
        game.startNewRound("GRAAL");

        List<Game> gameList = List.of(game);

        Mockito.when(gameRepository.findAll())
                .thenReturn(gameList);

        assertEquals(1, service.getAllGames().size());
    }

    private static ProgressPresentationDTO convertGameToProgressDTO(Game game) {
        return new ProgressPresentationDTO.Builder(game.getId())
                .gameStatus(game.getGameStatus().getStatus())
                .score(game.getScore())
                .currentHint(game.getLatestRound().giveHint())
                .feedbackHistory(game.getLatestRound().getFeedbackHistory())
                .build();
    }
}