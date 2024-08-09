package com.example.farmacia.controller;

import com.example.farmacia.model.Produto;
import com.example.farmacia.service.ProdutoService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping("/index")
    public String listarProdutos(Model model, HttpServletResponse response, HttpSession session) {
        
        String visitaDataHora = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        Cookie cookie = new Cookie("visita", visitaDataHora);
        cookie.setMaxAge(24 * 60 * 60); 
        cookie.setPath("/"); 
        response.addCookie(cookie);

        List<Produto> produtos = produtoService.findAllNotDeleted();
        model.addAttribute("produtos", produtos);

        
        @SuppressWarnings("unchecked")
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");

        if (carrinho == null) {
            carrinho = new ArrayList<>();
        }

        model.addAttribute("quantidadeItensCarrinho", carrinho.size());

        return "index";
    }

    @GetMapping("/verCarrinho")
    public String verCarrinho(Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");

        if (carrinho == null) {
            carrinho = new ArrayList<>();
        }

        double valorTotal = 0.0;

        
        for (Produto produto : carrinho) {
            valorTotal += produto.getPreco();
        }

        model.addAttribute("carrinho", carrinho);
        model.addAttribute("valorTotal", valorTotal);

        model.addAttribute("carrinho", carrinho);
        model.addAttribute("quantidadeItensCarrinho", carrinho.size());

        return "verCarrinho"; 
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        List<Produto> produtos = produtoService.findAllNotDeleted();
        model.addAttribute("produtos", produtos);
        return "admin";
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    @GetMapping("/editar")
    public String editarProduto(@RequestParam("id") Long id, Model model) {
        Produto produto = produtoService.findById(id);
        if (produto != null) {
            model.addAttribute("produto", produto);
            return "editarProduto"; 
        } else {
            
            return "redirect:/admin"; 
        }
    }

    @PostMapping("/atualizarProduto")
    public String atualizarProduto(@Valid Produto produto, BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "editarProduto"; 
        }
        produtoService.update(produto);
        redirectAttributes.addFlashAttribute("message", "Produto atualizado com sucesso!");
        return "redirect:/admin"; 
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    @GetMapping("/deletar")
    public String deletarProduto(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        produtoService.deletarProduto(id);
        redirectAttributes.addFlashAttribute("mensagem", "Produto removido com sucesso!");
        return "redirect:/admin";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("produto", new Produto());
        return "cadastro";
    }

    @PostMapping("/salvar")
    public String salvarProduto(@Valid Produto produto, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "cadastro"; // Redireciona de volta para a página de cadastro se houver erros
        }
        produtoService.save(produto);
        redirectAttributes.addFlashAttribute("message", "Produto salvo com sucesso!");
        return "redirect:/admin"; // Redireciona para a página de administração após salvar
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    @GetMapping("/adicionarCarrinho")
    public String adicionarCarrinho(@RequestParam("id") Long id, HttpSession session) {
        
        Produto produto = produtoService.findById(id);

        if (produto != null) {
          
            @SuppressWarnings("unchecked")
            List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");
            if (carrinho == null) {
                carrinho = new ArrayList<>();
                session.setAttribute("carrinho", carrinho);
            }

            
            carrinho.add(produto);
        }

        
        return "redirect:/index";
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    @GetMapping("/finalizarCompra")
    public String finalizarCompra(HttpSession session, Model model) {
       
        @SuppressWarnings("unchecked")
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");

        double valorTotal = 0.0;

       
        if (carrinho != null) {
            for (Produto produto : carrinho) {
                valorTotal += produto.getPreco();
                produto.setEstoque(produto.getEstoque() - 1);
                produtoService.update(produto);
            }
            // Limpar o carrinho
            session.removeAttribute("carrinho");
        }

       
        model.addAttribute("valorTotal", valorTotal);

        // Invalidar a sessão
        session.invalidate();

       
        return "redirect:/index";
    }

    // ----------------------------------------------------------------------------------------------------------------------------

}
