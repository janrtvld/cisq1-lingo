package nl.hu.cisq1.lingo.trainer.presentation;


import javassist.NotFoundException;
import nl.hu.cisq1.lingo.trainer.application.GameService;
import nl.hu.cisq1.lingo.trainer.domain.exception.GameNotFoundException;
import nl.hu.cisq1.lingo.trainer.domain.exception.GameStateException;
import nl.hu.cisq1.lingo.trainer.presentation.dto.ProgressPresentationDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import javax.persistence.EntityNotFoundException;
import java.util.List;


@RestController
@RequestMapping("/lingo")
public class GameController {
    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

   // TODO: Error responses niet direct doorgeven?

    @PostMapping("start")
    public ProgressPresentationDTO startGame() {
        return this.service.startGame();
    }

    @PostMapping("/{id}/newRound")
    public ProgressPresentationDTO startRound(@PathVariable("id") Long id) {
        try {
            return this.service.startNewRound(id);
        } catch (GameNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (GameStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ProgressPresentationDTO getGameProgress(@PathVariable("id") Long id) {
        try {
            return this.service.getProgress(id);
        } catch (GameNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @PostMapping("/{id}/guess")
    public ProgressPresentationDTO guess(@PathVariable("id") Long id, @RequestParam String attempt) {
        try {
            return this.service.guess(id,attempt);
        } catch (GameNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (GameStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
    }

    @GetMapping("games")
    public List<ProgressPresentationDTO> getAllGames() {
        try {
            return this.service.getAllGames();
        } catch (NotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

}
