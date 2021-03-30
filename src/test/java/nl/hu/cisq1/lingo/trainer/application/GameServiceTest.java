package nl.hu.cisq1.lingo.trainer.application;

import javassist.NotFoundException;
import nl.hu.cisq1.lingo.trainer.data.SpringGameRepository;
import nl.hu.cisq1.lingo.trainer.domain.Game;
import nl.hu.cisq1.lingo.trainer.domain.exception.GameNotFoundException;
import nl.hu.cisq1.lingo.trainer.domain.exception.NoActiveRoundsException;
import nl.hu.cisq1.lingo.trainer.presentation.dto.GamePresentationDTO;
import nl.hu.cisq1.lingo.trainer.presentation.dto.ProgressPresentationDTO;
import nl.hu.cisq1.lingo.words.data.SpringWordRepository;
import nl.hu.cisq1.lingo.words.domain.Word;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    @DisplayName("init")
    void init() {
        gameRepository = Mockito.mock(SpringGameRepository.class);
        wordRepository = Mockito.mock(SpringWordRepository.class);
        service = new GameService(gameRepository,wordRepository);

    }

    private GamePresentationDTO convertGameToGameDTO(Game game) {
        return new GamePresentationDTO.Builder(game.getId())
                .score(game.getProgress().getScore())
                .gameStatus(game.getGameStatus().toString())
                .build();
    }

    private static ProgressPresentationDTO convertGameToProgressDTO(Game game) {
        return new ProgressPresentationDTO.Builder(game.getId())
                .score(game.getProgress().getScore())
                .newHint(game.getLatestRound().giveHint())
                .feedbackHistory(game.getLatestRound().getFeedbackHistory())
                .build();

    }

    @Test
    @DisplayName("Starting a game returns game DTO object")
    void startGameReturnsNewGame() {
        Game game = new Game();

        Mockito.when(gameRepository.save(game))
                .thenReturn(game);

        GamePresentationDTO result = service.startGame();

        GamePresentationDTO expected = convertGameToGameDTO(game);

        assertEquals(expected.gameStatus, result.gameStatus);
        assertEquals(expected.id, result.id);
        assertEquals(expected.score, result.score);
    }

    @Test
    @DisplayName("Get progress throws exception if game not found")
    void getProgressReturnsErrorIfGameNotFound() {
        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.getProgress(0L));
    }

    @Test
    @DisplayName("Get progress throws exception if there is no progress yet")
    void getProgressReturnsErrorIfGameHasNoProgress() {
        Game game = new Game();
        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.of(game));

        assertThrows(NoActiveRoundsException.class, () -> service.getProgress(0L));
    }

    @ParameterizedTest
    @DisplayName("Get progress returns current state of the game")
    @MethodSource("randomGameExamples")
    void getProgressReturnsCurrentGameState(Game game, ProgressPresentationDTO expected) {
        Mockito.when(gameRepository.findById(game.getId()))
                .thenReturn(Optional.of(game));

        ProgressPresentationDTO result = service.getProgress(game.getId());

        assertEquals(expected.id, result.id);
        assertEquals(expected.score, result.score);
        assertEquals(expected.feedbackHistory, result.feedbackHistory);
        assertEquals(expected.newHint, result.newHint);
    }

    static Stream<Arguments> randomGameExamples() {
        Game gameWithPlayingRound = new Game();
        Word wordToGuess = new Word("tower");
        gameWithPlayingRound.startNewRound(wordToGuess);
        ProgressPresentationDTO gameWithPlayingRoundProgress = convertGameToProgressDTO(gameWithPlayingRound);

        Game gameWithWordGuessed = new Game();
        gameWithWordGuessed.startNewRound(wordToGuess);
        gameWithWordGuessed.guess("tower");
        ProgressPresentationDTO gameWithWordGuessedProgress = convertGameToProgressDTO(gameWithWordGuessed);

        Game gameWithPlayerEliminated = new Game();
        gameWithPlayerEliminated.startNewRound(wordToGuess);
        gameWithPlayerEliminated.guess("stupid");
        gameWithPlayerEliminated.guess("stupid");
        gameWithPlayerEliminated.guess("stupid");
        gameWithPlayerEliminated.guess("stupid");
        gameWithPlayerEliminated.guess("stupid");
        ProgressPresentationDTO gameWithPlayerEliminatedProgress = convertGameToProgressDTO(gameWithPlayerEliminated);

        return Stream.of(
                Arguments.of(gameWithPlayingRound, gameWithPlayingRoundProgress),
                Arguments.of(gameWithWordGuessed, gameWithWordGuessedProgress),
                Arguments.of(gameWithPlayerEliminated, gameWithPlayerEliminatedProgress)
        );
    }

    @Test
    @DisplayName("New round throws error if game does not exists")
    void newRoundThrowsErrorByNonExistingGame() {
        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.startNewRound(anyLong()));
    }

    @Test
    @DisplayName("New round returns progress of game")
    void newRoundReturnsGameProgress() {
        // Arrange / Act / Assert
        Game game = new Game();
        Word wordToGuess = new Word("tower");
        game.startNewRound(wordToGuess);
        ProgressPresentationDTO expected = convertGameToProgressDTO(game);

        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Game()));

        Mockito.when(wordRepository.findRandomWordByLength(anyInt()))
                .thenReturn(Optional.of(new Word("tower")));

        ProgressPresentationDTO result = service.startNewRound(anyLong());

        assertEquals(expected.id, result.id);
        assertEquals(expected.score, result.score);
        assertEquals(expected.feedbackHistory, result.feedbackHistory);
        assertEquals(expected.newHint, result.newHint);
    }

    @Test
    @DisplayName("Guess can not be done if game does not exists")
    void guessThrowsExceptionByNonExistingGame() {
        // Arrange / Act / Assert
        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> service.guess(anyLong(),"LOSER"));

    }

    @Test
    @DisplayName("Guess is saved and returned in Progress")
    void guessIsSavedInGameProgress() {
        // Arrange / Act / Assert
        Game game = new Game();
        Word wordToGuess = new Word("tower");
        game.startNewRound(wordToGuess);

        Mockito.when(gameRepository.findById(anyLong()))
                .thenReturn(Optional.of(game));

        ProgressPresentationDTO result = service.guess(anyLong(),"lower");

        game.guess("lower");
        ProgressPresentationDTO expected = convertGameToProgressDTO(game);

        assertEquals(expected.feedbackHistory, result.feedbackHistory);
    }

    @Test
    @DisplayName("All games throws exception by no games")
    void allGamesEmptyException() {
        // Arrange / Act / Assert
        List<Game> gameList = new ArrayList<>();
        Mockito.when(gameRepository.findAll())
                .thenReturn(gameList);

        assertThrows(NotFoundException.class, () -> service.getAllGames());
    }

    @Test
    @DisplayName("All games throws exception by no games")
    void allGamesReturnsListOfGamesDTO() throws NotFoundException {
        Game game = new Game();
        List<Game> gameList = new ArrayList<>();
        gameList.add(game);

        Mockito.when(gameRepository.findAll())
                .thenReturn(gameList);

        List<GamePresentationDTO> expected = List.of(convertGameToGameDTO(game));
        assertEquals(expected.size(), service.getAllGames().size());
    }

    @Test
    @DisplayName("Guess can not be done if player is eliminated")
    void guessThrowsExceptionByEliminatedPlayer() {
        // Arrange / Act / Assert

    }

    @Test
    @DisplayName("New round throws error if player is eliminated")
    void newRoundThrowsExceptionByEliminatedPlayer() {
        // Arrange / Act / Assert

    }

    @Test
    @DisplayName("New round starts round with correct word length")
    void newRoundCorrectWordLength() {
        // Arrange / Act / Assert

    }
}