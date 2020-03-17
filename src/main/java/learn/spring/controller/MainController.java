package learn.spring.controller;

import learn.spring.domain.Message;
import learn.spring.domain.User;
import learn.spring.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Controller
public class MainController {
    @Autowired
    private MessageRepository messageRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping
    public String greeting() {
        return "redirect:/main";
    }

    @GetMapping("/main")
    public String main(@RequestParam(required = false) String filter, Model model) {
        Iterable<Message> messages;
        if (filter == null || filter.isEmpty())
            messages = messageRepository.findAll();
        else
            messages = messageRepository.findByTag(filter);
        model.addAttribute("messages", messages);
        if (filter != null)
            model.addAttribute("filter", filter);
        return "main";
    }

    @PostMapping("/main")
    public String add(@RequestParam String text,
                      @RequestParam String tag,
                      @AuthenticationPrincipal User user,
                      @RequestParam("file") MultipartFile file,
                      Model model) throws IOException {
        Message message = new Message(text, tag, user);
        if (file != null && !file.isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (uploadDir.exists()) uploadDir.mkdir();
            String resultFilename = UUID.randomUUID().toString() + "." + file.getOriginalFilename();
            file.transferTo(new File(uploadPath + resultFilename));
            message.setFileName(resultFilename);
        }
        messageRepository.save(message);
        model.addAttribute("messages", messageRepository.findAll());
        return "main";
    }
}
