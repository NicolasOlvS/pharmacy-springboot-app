package com.example.farmacia.service;

import com.example.farmacia.model.Produto;
import com.example.farmacia.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public List<Produto> findAllNotDeleted() {
        return produtoRepository.findByIsDeletedFalse();
    }
    
     
     public Produto findById(Long id) {
        
        Optional<Produto> produto = produtoRepository.findById(id);
       
        return produto.orElse(null);
    }

  
    public void deletarProduto(Long id) {
    
        Produto produto = findById(id);
        if (produto != null) {
   
            produto.setIsDeleted(true);
            produtoRepository.save(produto);
        }
    }

    public Produto save(Produto produto) {
        return produtoRepository.save(produto);
    }

    public void update(Produto produto) {
  
        if (produtoRepository.existsById(produto.getId())) {
            produtoRepository.save(produto);
        }
    }

}
