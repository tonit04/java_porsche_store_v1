package vn.tonnguyen.porsche_store_v1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.tonnguyen.porsche_store_v1.model.Cart;
import vn.tonnguyen.porsche_store_v1.model.CartDetail;
import vn.tonnguyen.porsche_store_v1.model.User;
import vn.tonnguyen.porsche_store_v1.service.interf.CarService;
import vn.tonnguyen.porsche_store_v1.service.interf.CartDetailService;
import vn.tonnguyen.porsche_store_v1.service.interf.CartService;
import vn.tonnguyen.porsche_store_v1.service.interf.UserService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final UserService userService;
    private final CarService carService;
    private final CartDetailService cartDetailService;

    @Autowired
    public CartController(CartService cartService, UserService userService, CarService carService, CartDetailService cartDetailService) {
        this.cartService = cartService;
        this.userService = userService;
        this.carService = carService;
        this.cartDetailService = cartDetailService;
    }

    @GetMapping()
    public String showCart(
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            redirectAttributes.addFlashAttribute("inforMessage", "You need to login first");
            return "redirect:/login";
        }
        String username = principal.getName();
        Cart cart = cartService.findByUser(userService.findByUsername(username));
        if (cart == null) {
            redirectAttributes.addFlashAttribute("inforMessage", "Your cart is empty,start shopping now");
            return "cart/list";
        } else {
            model.addAttribute("cartDetails", cartDetailService.findByCart(cart));
            return "cart/list";
        }
    }

    @PostMapping("/add/{id}")
    public String addToCart(
            Model model,
            @PathVariable("id") Integer carId,
            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
            Principal principal,
            RedirectAttributes redirectAttributes,
            @RequestHeader(value = "referer", required = false) String referer) {

        if (principal == null) {
            redirectAttributes.addFlashAttribute("inforMessage", "You need to login first");
            return "redirect:/login";
        }
        String username = principal.getName();
        try {
            cartService.addToCart(username, carId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully added to the cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/delete/{id}")
    public String deleteFromCart(
            @PathVariable("id") Integer carId,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            if (principal != null) {
                String username = principal.getName();
                User user = userService.findByUsername(username);
                Cart cart = cartService.findByUser(user);
                if (cart != null) {
                    CartDetail detail = cartDetailService.findByCartAndCarId(cart, carId);
                    if (detail != null) {
                        cartDetailService.deleteByIdCustom(detail.getId()); // ✅ xóa qua ID
                        redirectAttributes.addFlashAttribute("successMessage", "Successfully deleted from the cart");
                        return "redirect:/cart";
                    }
                }
            } else {
                redirectAttributes.addFlashAttribute("infoMessage", "You need to login first");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/";
    }


}
