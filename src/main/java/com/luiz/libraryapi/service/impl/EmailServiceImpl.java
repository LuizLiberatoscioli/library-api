package com.luiz.libraryapi.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.luiz.libraryapi.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
	
	@Value("${application.mail.default-remetent}")
	private String remetent;
	
	private final JavaMailSender javaMailSender;

	@Override
	public void sendMails(String mensage, List<String> mailsList) {
		
		String[] mails = mailsList.toArray(new String [mailsList.size()]);
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(remetent);
		mailMessage.setSubject("Livro com empréstimo atrasado");
		mailMessage.setText(mensage);
		
		mailMessage.setTo( mails);
		
		javaMailSender.send(mailMessage);
		
		
	}

}
