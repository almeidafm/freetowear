package com.freetowear.freetowear.controller.api.admin;

import com.freetowear.freetowear.model.Cupom;
import com.freetowear.freetowear.repository.CupomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * CouponController — manages discount coupons.
 * POST   /coupon/create ✔
 * GET    /coupon ⏳
 * GET    /coupon/{id} ⏳
 * PATCH  /coupon/{id} ⏳ - must complete all fields to work, I shall change that later.
 * */
@Controller
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CupomRepository couponRepository;

    @PostMapping("/create")
    public String createCoupon(
            @RequestParam String codigo,
            @RequestParam(required = false) String descricao,
            @RequestParam Cupom.TipoDesconto tipoDesconto,
            @RequestParam BigDecimal valorDesconto,
            @RequestParam(required = false) BigDecimal valorMinimoPedido,
            @RequestParam String dataInicio,
            @RequestParam String dataFim
    ) {

        Cupom cupom = new Cupom();
        cupom.setCodigo(codigo);
        cupom.setDescricao(descricao);
        cupom.setTipo_desconto(tipoDesconto);
        cupom.setValor_desconto(valorDesconto);
        cupom.setValor_minimo_pedido(valorMinimoPedido);
        cupom.setData_inicio(LocalDate.parse(dataInicio));
        cupom.setData_fim(LocalDate.parse(dataFim));
        cupom.setAtivo(true);

        couponRepository.save(cupom);

        return "redirect:/";
    }

    @PatchMapping("/{id}")
    public String updateCoupon(
            @PathVariable Integer id,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) Cupom.TipoDesconto tipoDesconto,
            @RequestParam(required = false) BigDecimal valorDesconto,
            @RequestParam(required = false) BigDecimal valorMinimoPedido,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) Boolean ativo
    ) {
        couponRepository.findById(id).ifPresent(cupom -> {
            if (codigo != null) cupom.setCodigo(codigo);
            if (descricao != null) cupom.setDescricao(descricao);
            if (tipoDesconto != null) cupom.setTipo_desconto(tipoDesconto);
            if (valorDesconto != null) cupom.setValor_desconto(valorDesconto);
            if (valorMinimoPedido != null) cupom.setValor_minimo_pedido(valorMinimoPedido);
            if (dataInicio != null) cupom.setData_inicio(LocalDate.parse(dataInicio));
            if (dataFim != null) cupom.setData_fim(LocalDate.parse(dataFim));
            if (ativo != null) cupom.setAtivo(ativo);

            couponRepository.save(cupom);
        });

        return "redirect:/";
    }
}