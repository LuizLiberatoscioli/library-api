package com.luiz.libraryapi.resource;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.print.Pageable;
import java.util.Arrays;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luiz.libraryapi.api.dto.BookDTO;
import com.luiz.libraryapi.api.resouce.BookController;
import com.luiz.libraryapi.exception.BusinessException;
import com.luiz.libraryapi.model.entity.Book;
import com.luiz.libraryapi.service.BookService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")  
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {
	
	//rota
	static String BOOK_API = "/api/books";
	
	@Autowired
	MockMvc mvc;
	
	@MockBean
	BookService service ;
	
	@Test
	@DisplayName ("Deve criar um livro com sucesso.")
	public void createBookTest() throws Exception  {
			
		BookDTO dto = createNewBook();
		Book savedBook = Book.builder().id(10l).author("Arthur").title("as aventuras").isbn("001").build();
		
		BDDMockito.given(service.save(Mockito.any(Book.class)))
			.willReturn(savedBook);		
		String json = new ObjectMapper().writeValueAsString(dto);	//criar o json , para nao precisar escrever ele todo na string
		
		//montar a requisicao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders		
		.post(BOOK_API) //tipo requisicao
		.contentType(MediaType.APPLICATION_JSON)  	//conteudo
		.accept(MediaType.APPLICATION_JSON) 		// o servidor aceita tipo json
		.content(json);								// passar o json (corpo da requiscao , dados do livro)
		
		//fazer a requisicao
		mvc							
		.perform(request)	// recebe a requisicao q preparamos
		.andExpect(MockMvcResultMatchers.status().isCreated())				//mandar os verificadores
		.andExpect(jsonPath("id").value(10l))							//testar o json de resposta com os dados 
		 .andExpect( jsonPath("title").value(dto.getTitle()) )
		 .andExpect( jsonPath("author").value(dto.getAuthor()) )
		 .andExpect( jsonPath("isbn").value(dto.getIsbn()) )
		;
	}
	
	@Test
	@DisplayName ("Deve lancar erro de validacao quando nao houver dados suficientes para criacao do livro")
	public void createInvalidTest() throws Exception {
		
		String json = new ObjectMapper().writeValueAsString(new BookDTO());	
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders		
				.post(BOOK_API) //tipo requisicao
				.contentType(MediaType.APPLICATION_JSON)  	
				.accept(MediaType.APPLICATION_JSON) 		
				.content(json);		
		
		mvc.perform(request)
		.andExpect ( status().isBadRequest() ) 
		.andExpect ( jsonPath ("errors" , Matchers.hasSize(3)));
	}
	
	@Test
	@DisplayName ("Deve lançar erro ao tentar cadastrar um livro com isbn ja utiliado por outro.")
	public void createBookWithDuplicatedIsbn ()  throws Exception{
		
		BookDTO dto = createNewBook();
		
		String json = new ObjectMapper().writeValueAsString(dto);	
		String mensagemErro = "Isbn ja cadastrado."; 
		BDDMockito.given(service.save(Mockito.any(Book.class)))
			.willThrow(new BusinessException ("Isbn ja cadastrado."));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders		
				.post(BOOK_API) //tipo requisicao
				.contentType(MediaType.APPLICATION_JSON)  	
				.accept(MediaType.APPLICATION_JSON) 		
				.content(json);	
		
		mvc.perform(request)
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("errors" , Matchers.hasSize(1))) 
		.andExpect(jsonPath ("errors[0]").value(mensagemErro));
	}
	
	@Test
    @DisplayName("Deve obter informacoes de um livro.")
    public void getBookDetailsTest() throws Exception{
        //cenario (given)
        Long id = 1l;

        Book book = Book.builder()
                    .id(id)
                    .title(createNewBook().getTitle())
                    .author(createNewBook().getAuthor())
                    .isbn(createNewBook().getIsbn())
                    .build();

        BDDMockito.given( service.getById(id) ).willReturn(Optional.of(book));

        //execucao (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
            .perform(request)
            .andExpect(status().isOk())
            .andExpect( jsonPath("id").value(id) )
            .andExpect( jsonPath("title").value(createNewBook().getTitle()) )
            .andExpect( jsonPath("author").value(createNewBook().getAuthor()) )
            .andExpect( jsonPath("isbn").value(createNewBook().getIsbn()) )
        ;
    }
	
	   @Test
	    @DisplayName("Deve retornar resource not found quando o livro procurado não existir")
	    public void bookNotFoundTest() throws Exception {

	        BDDMockito.given( service.getById(Mockito.anyLong()) ).willReturn( Optional.empty() );

	        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
	                .get(BOOK_API.concat("/" + 1))
	                .accept(MediaType.APPLICATION_JSON);

	        mvc
	            .perform(request)
	            .andExpect(status().isNotFound());
	    }
	   
	   @Test
	    @DisplayName("Deve deletar um livro")
	    public void deleteBookTest() throws Exception {

	        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.of(Book.builder().id(1l).build()));

	        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
	                .delete(BOOK_API.concat("/" + 1));

	        mvc.perform( request )
	            .andExpect( status().isNoContent() );
	    }
	   
	   @Test
	    @DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar")
	    public void deleteInexistentBookTest() throws Exception {

	        BDDMockito.given(service.getById(anyLong())).willReturn(Optional.empty());

	        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
	                .delete(BOOK_API.concat("/" + 1));

	        mvc.perform( request )
	                .andExpect( status().isNotFound() );
	    }
	   
	   @Test
	    @DisplayName("Deve atualizar um livro")
	    public void updateBookTest1() throws Exception {
	        Long id = 1l;
	        String json = new ObjectMapper().writeValueAsString(createNewBook());

	        Book updatingBook = Book.builder().id(1l).title("some title").author("some author").isbn("321").build();
	        BDDMockito.given( service.getById(id) ).willReturn( Optional.of(updatingBook) );
	        Book updatedBook = Book.builder().id(id).author("Artur").title("As aventuras").isbn("321").build();
	        BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);

	        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
	                .put(BOOK_API.concat("/" + 1))
	                .content(json)
	                .accept(MediaType.APPLICATION_JSON)
	                .contentType(MediaType.APPLICATION_JSON);

	        mvc.perform( request )
	                .andExpect( status().isOk() )
	                .andExpect( jsonPath("id").value(id) )
	                .andExpect( jsonPath("title").value(createNewBook().getTitle()) )
	                .andExpect( jsonPath("author").value(createNewBook().getAuthor()) )
	                .andExpect( jsonPath("isbn").value("321") );
	    }
	   @Test
	    @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
	    public void updateInexistentBookTest1() throws Exception {

	        String json = new ObjectMapper().writeValueAsString(createNewBook());
	        BDDMockito.given( service.getById(Mockito.anyLong()) )
	                .willReturn( Optional.empty() );

	        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
	                .put(BOOK_API.concat("/" + 1))
	                .content(json)
	                .accept(MediaType.APPLICATION_JSON)
	                .contentType(MediaType.APPLICATION_JSON);

	        mvc.perform( request )
	                .andExpect( status().isNotFound() );
	    }
	   
	   @Test
	    @DisplayName("Deve atualizar um livro")
	    public void updateBookTest() throws Exception {
	        Long id = 1l;
	        String json = new ObjectMapper().writeValueAsString(createNewBook());

	        Book updatingBook = Book.builder().id(1l).title("some title").author("some author").isbn("321").build();
	        BDDMockito.given( service.getById(id) ).willReturn( Optional.of(updatingBook) );
	        Book updatedBook = Book.builder().id(id).author("Artur").title("As aventuras").isbn("321").build();
	        BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);

	        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
	                .put(BOOK_API.concat("/" + 1))
	                .content(json)
	                .accept(MediaType.APPLICATION_JSON)
	                .contentType(MediaType.APPLICATION_JSON);

	        mvc.perform( request )
	                .andExpect( status().isOk() )
	                .andExpect( jsonPath("id").value(id) )
	                .andExpect( jsonPath("title").value(createNewBook().getTitle()) )
	                .andExpect( jsonPath("author").value(createNewBook().getAuthor()) )
	                .andExpect( jsonPath("isbn").value("321") );
	    }
	   
	    @Test
	    @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
	    public void updateInexistentBookTest() throws Exception {

	        String json = new ObjectMapper().writeValueAsString(createNewBook());
	        BDDMockito.given( service.getById(Mockito.anyLong()) )
	                .willReturn( Optional.empty() );

	        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
	                .put(BOOK_API.concat("/" + 1))
	                .content(json)
	                .accept(MediaType.APPLICATION_JSON)
	                .contentType(MediaType.APPLICATION_JSON);

	        mvc.perform( request )
	                .andExpect( status().isNotFound() );
	    }
	
	  
	   @Test
	   @DisplayName("Deve filtrar books.")
	   public void findBooksTest() throws Exception {
		   Long id = 1l;
		   
		   Book book = Book.builder().id(1l).title("some title").author("some author").isbn("321").build();
		   
		   BDDMockito.given( service.find(Mockito.any(Book.class), (org.springframework.data.domain.Pageable) Mockito.any(Pageable.class)) )
           .willReturn( new PageImpl<Book>( Arrays.asList(book), PageRequest.of(0,100), 1 )   );
		   
		   
		   String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());
		   
		   MockHttpServletRequestBuilder request = MockMvcRequestBuilders
		   .get(BOOK_API.concat(queryString)).accept(MediaType.APPLICATION_JSON);
		   
		   mvc
		   .perform(request)
		   .andExpect( status().isOk())
		   .andExpect(jsonPath("content", Matchers.hasSize(1)))
		   .andExpect(jsonPath ("totalElements").value(1))
		   .andExpect(jsonPath("pageable.pageSize").value(100))
		   .andExpect(jsonPath("pageable.pageNumber").value(0));
		   
	   }
	
	   private BookDTO createNewBook() {
			return BookDTO.builder().author("Arthur").title("as aventuras").isbn("001").build();
	   }
}
