package com.hoaxify.ws.hoax;

import com.hoaxify.ws.hoax.vm.HoaxSubmitVM;
import com.hoaxify.ws.hoax.vm.HoaxVM;
import com.hoaxify.ws.shared.CurrentUser;
import com.hoaxify.ws.shared.GenericResponse;
import com.hoaxify.ws.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/1.0")
public class HoaxController {

    HoaxService hoaxService;

    public HoaxController(HoaxService hoaxService) {
        this.hoaxService = hoaxService;
    }

    @PostMapping("/hoaxes")
    public GenericResponse saveHoax(@Valid @RequestBody HoaxSubmitVM hoax, @CurrentUser User user) {
        hoaxService.saveHoax(hoax, user);
        return new GenericResponse("hoax is saved");
    }

    @GetMapping("/hoaxes")
    Page<HoaxVM> getHoaxes(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page) {
        return hoaxService.getHoaxes(page).map(HoaxVM::new);
    }

    @GetMapping({"/hoaxes/{id:[0-9]+}", "/users/{username}/hoaxes/{id:[0-9]+}"})
    ResponseEntity<?> getHoaxesRelative(@RequestParam(name = "count", required = false, defaultValue = "false") boolean count,
                                        @PathVariable Long id,
                                        @PathVariable(required = false) String username,
                                        @RequestParam(name = "direction", defaultValue = "before") String direction,
                                        @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page) {
        if (count) {
            long newHoaxCount = hoaxService.getNewHoaxesCount(id, username);
            Map<String, Long> response = new HashMap<>();
            response.put("count", newHoaxCount);
            return ResponseEntity.ok(response);
        }
        if (direction.equals("after")) {
            List<HoaxVM> newHoaxes = hoaxService.getNewHoaxes(id, username, page.getSort()).stream()
                    .map(HoaxVM::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(newHoaxes);
        }

        return ResponseEntity.ok(hoaxService.getOldHoaxes(id, page, username).map(HoaxVM::new));
    }

    @GetMapping("/users/{username}/hoaxes")
    Page<HoaxVM> getUserHoaxes(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable page,
                               @PathVariable("username") String username) {
        return hoaxService.getHoaxesOfUser(username, page).map(HoaxVM::new);
    }


    @DeleteMapping("/hoaxes/{id:[0-9]+}")
    @PreAuthorize("@hoaxSecurity.isAllowedToDelete(#id,principal)") //principal loggedInUser'ı ifade eder
    public GenericResponse deleteHoax(@PathVariable long id) {
        hoaxService.delete(id);
        return new GenericResponse("Hoax removed");
    }


}
