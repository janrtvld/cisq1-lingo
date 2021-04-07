package nl.hu.cisq1.lingo.trainer.presentation;


import javassist.NotFoundException;
import nl.hu.cisq1.lingo.trainer.application.GameService;
import nl.hu.cisq1.lingo.trainer.domain.exception.GameNotFoundException;
import nl.hu.cisq1.lingo.trainer.domain.exception.GameStateException;
import nl.hu.cisq1.lingo.trainer.application.dto.ProgressDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;


@RestController
@RequestMapping("/lingo")
public class GameController {
    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @PostMapping("start")
    public ProgressDTO startGame() {
        return this.service.startGame();
    }

    @PostMapping("/{id}/newRound")
    public ProgressDTO startRound(@PathVariable("id") Long id) {
        try {
            return this.service.startNewRound(id);
        } catch (GameNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (GameStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ProgressDTO getGameProgress(@PathVariable("id") Long id) {
        try {
            return this.service.getProgress(id);
        } catch (GameNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @PostMapping("/{id}/guess")
    public ProgressDTO guess(@PathVariable("id") Long id, @RequestParam String attempt) {
        try {
            return this.service.guess(id,attempt);
        } catch (GameNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (GameStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("games")
    public List<ProgressDTO> getAllGames() {
        try {
            return this.service.getAllGames();
        } catch (NotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

}
