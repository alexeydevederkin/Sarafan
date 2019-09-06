package com.asd.sarafan.service;

import com.asd.sarafan.domain.Message;
import com.asd.sarafan.domain.User;
import com.asd.sarafan.domain.UserSubscription;
import com.asd.sarafan.domain.Views;
import com.asd.sarafan.dto.EventType;
import com.asd.sarafan.dto.MessagePageDto;
import com.asd.sarafan.dto.MetaDto;
import com.asd.sarafan.dto.ObjectType;
import com.asd.sarafan.repo.MessageRepo;
import com.asd.sarafan.repo.UserSubscriptionRepo;
import com.asd.sarafan.util.WsSender;
import org.apache.logging.log4j.util.TriConsumer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private static String URL_PATTERN = "https?://?[\\w\\d._\\-%/?=&#]+";
    private static String IMAGE_PATTERN = "\\.(jpeg|jpg|gif|png)$";

    private static Pattern URL_REGEX = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);
    private static Pattern IMG_REGEX = Pattern.compile(IMAGE_PATTERN, Pattern.CASE_INSENSITIVE);

    private final MessageRepo messageRepo;
    private final UserSubscriptionRepo userSubscriptionRepo;
    private final TriConsumer<String, EventType, Message> wsSender;

    @Autowired
    public MessageService(MessageRepo messageRepo,
                          UserSubscriptionRepo userSubscriptionRepo,
                          WsSender wsSender) {
        this.messageRepo = messageRepo;
        this.userSubscriptionRepo = userSubscriptionRepo;
        this.wsSender = wsSender.getSender(ObjectType.MESSAGE, Views.FullMessage.class);
    }


    private void fillMeta(Message message) throws IOException {
        String text = message.getText();
        Matcher matcher = URL_REGEX.matcher(text);

        if (matcher.find()) {
            String url = text.substring(matcher.start(), matcher.end());

            matcher = IMG_REGEX.matcher(url);

            message.setLink(url);

            if (matcher.find()) {
                message.setLinkCover(url);
            } else if (!url.contains("youtu")) {
                MetaDto meta = getMeta(url);

                message.setLinkCover(meta.getCover());
                message.setLinkTitle(meta.getTitle());
                message.setLinkDescription(meta.getDescription());
            }
        }
    }

    private MetaDto getMeta(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        Elements title = doc.select("meta[name$=title],meta[property$=title]");
        Elements description = doc.select("meta[name$=description],meta[property$=description]");
        Elements cover = doc.select("meta[name$=image],meta[property$=image]");

        return new MetaDto(
                getContent(title.first()),
                getContent(description.first()),
                getContent(cover.first())
        );
    }

    private String getContent(Element element) {
        return element == null ? "" : element.attr("content");
    }

    public void delete(Message message) {
        messageRepo.delete(message);

        // Send info about deleted message via websocket back to the author
        wsSender.accept(message.getAuthor().getId(), EventType.REMOVE, message);

        // Send info about deleted message via websocket to every subscriber of the author
        for (UserSubscription subscription : userSubscriptionRepo.findByChannel(message.getAuthor())) {
            if (subscription.isActive()) {
                wsSender.accept(subscription.getSubscriber().getId(), EventType.REMOVE, message);
            }
        }
    }

    public Message update(Message messageFromDb, Message message) throws IOException {
        messageFromDb.setText(message.getText());

        fillMeta(messageFromDb);
        Message updatedMessage = messageRepo.save(messageFromDb);

        // Send updated message via websocket back to the author
        wsSender.accept(updatedMessage.getAuthor().getId(), EventType.UPDATE, updatedMessage);

        // Send updated message via websocket to every subscriber of the author
        for (UserSubscription subscription : userSubscriptionRepo.findByChannel(updatedMessage.getAuthor())) {
            if (subscription.isActive()) {
                wsSender.accept(subscription.getSubscriber().getId(), EventType.UPDATE, updatedMessage);
            }
        }

        return updatedMessage;
    }

    public Message create(Message message, User author) throws IOException {
        message.setCreationDate(LocalDateTime.now());
        fillMeta(message);
        message.setAuthor(author);
        Message updatedMessage = messageRepo.save(message);

        // Send created and updated message via websocket back to the author
        wsSender.accept(author.getId(), EventType.CREATE, updatedMessage);

        // Send created message via websocket to every subscriber of the author
        for (UserSubscription subscription : userSubscriptionRepo.findByChannel(author)) {
            if (subscription.isActive()) {
                wsSender.accept(subscription.getSubscriber().getId(), EventType.CREATE, updatedMessage);
            }
        }

        return updatedMessage;
    }

    public MessagePageDto findForUser(Pageable pageable, User user) {
        List<User> channels = userSubscriptionRepo.findBySubscriber(user)
                .stream()
                .filter(UserSubscription::isActive)
                .map(UserSubscription::getChannel)
                .collect(Collectors.toList());

        channels.add(user);

        Page<Message> page = messageRepo.findByAuthorIn(channels, pageable);

        return new MessagePageDto(
                page.getContent(),
                pageable.getPageNumber(),
                page.getTotalPages()
        );
    }
}