package com.hoaxify.ws.hoax;

import com.hoaxify.ws.file.FileAttachment;
import com.hoaxify.ws.file.FileAttachmentRepository;
import com.hoaxify.ws.file.FileService;
import com.hoaxify.ws.hoax.vm.HoaxSubmitVM;
import com.hoaxify.ws.user.User;
import com.hoaxify.ws.user.UserService;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class HoaxService {

    HoaxRepository hoaxRepository;
    UserService userService;
    FileAttachmentRepository fileAttachmentRepository;
    FileService fileService;

    public HoaxService(HoaxRepository hoaxRepository, UserService userService, FileService fileService,
                       FileAttachmentRepository fileAttachmentRepository) {
        this.hoaxRepository = hoaxRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileService = fileService;
        this.userService = userService;
    }

    public void saveHoax(HoaxSubmitVM hoaxSubmitVM, User user) {
        Hoax hoax = new Hoax();
        hoax.setContent(hoaxSubmitVM.getContent());
        hoax.setCreatedTimestamp(new Date());
        hoax.setUser(user);
        hoaxRepository.save(hoax);
        Optional<FileAttachment> optionalFileAttachment = fileAttachmentRepository.findById(hoaxSubmitVM.getAttachmentId());
        if (optionalFileAttachment.isPresent()) {
            FileAttachment fileAttachment = optionalFileAttachment.get();
            fileAttachment.setHoax(hoax);
            fileAttachmentRepository.save(fileAttachment);
        }
    }

    public Page<Hoax> getHoaxes(Pageable page) {

        return hoaxRepository.findAll(page);
    }

    public Page<Hoax> getHoaxesOfUser(String username, Pageable page) {
        User inDb = userService.getUserByUsername(username);
        return hoaxRepository.getHoaxesByUser(inDb, page);
    }

    public Page<Hoax> getOldHoaxes(Long id, Pageable page, String username) {
        Specification<Hoax> specification = idLessThan(id);
        if (username != null) {
            User inDB = userService.getUserByUsername(username);
            specification = specification.and(userIs(inDB));
        }
        return hoaxRepository.findAll(specification, page);
    }

    public long getNewHoaxesCount(Long id, String username) {
        Specification<Hoax> specification = idGreaterThan(id);
        if (username != null) {
            User inDB = userService.getUserByUsername(username);
            specification = specification.and(userIs(inDB));
        }
        return hoaxRepository.count(specification);
    }

    public List<Hoax> getNewHoaxes(Long id, String username, Sort sort) {
        Specification<Hoax> specification = idGreaterThan(id);
        if (username != null) {
            User inDB = userService.getUserByUsername(username);
            specification = specification.and(userIs(inDB));
        }
        return hoaxRepository.findAll(specification, sort);
    }

    public void delete(long id) {
        Hoax inDB = hoaxRepository.getOne(id);
        if (inDB.getFileAttachment() != null) {
            String filename = inDB.getFileAttachment().getName();
            fileService.deleteAttachmentFile(filename);
        }
        hoaxRepository.deleteById(id);
    }


    Specification<Hoax> idLessThan(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.lessThan(root.get("id"), id);
        };
    }

    Specification<Hoax> userIs(User user) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("user"), user);
        };
    }

    Specification<Hoax> idGreaterThan(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            return criteriaBuilder.greaterThan(root.get("id"), id);
        };
    }

}
