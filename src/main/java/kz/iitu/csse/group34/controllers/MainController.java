package kz.iitu.csse.group34.controllers;

import kz.iitu.csse.group34.entities.*;
import kz.iitu.csse.group34.repositories.*;
import kz.iitu.csse.group34.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.*;

@Controller
public class MainController {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserService userService;

    @GetMapping(value = "/")
    public String index(ModelMap model){
        List<Restaurants> restaurants = restaurantRepository.findAll();
        model.addAttribute("restaurant", restaurants);
        return "index";
    }

    @GetMapping(path = "/addRestaurant")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addRestaurant(Model model){
        return "addRestaurantPage";
    }

    @PostMapping(value = "/addRestaurant")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addRestaurant(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "category") String category

    ){
        restaurantRepository.save(new Restaurants(null, name, description,category));
        return "redirect:/";
    }
    @GetMapping(path = "/addFood/{restaurant_id}")
    public String addFood(ModelMap model, @PathVariable(name = "restaurant_id") Long restaurant_id){
        Restaurants restaurant = restaurantRepository.getOne(restaurant_id);
        model.addAttribute("restaurant", restaurant);
        return "addFoodPage";
    }

    @PostMapping(value = "/addFood")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR')")
    public String addFood(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "restaurant_id") Long restaurant_id,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "price") int price
    ){
        Restaurants restaurant = restaurantRepository.getOne(restaurant_id);

        foodRepository.save(new Food(null,name,restaurant,description,price));
        return "redirect:/";
    }

    @PostMapping(value = "/deleteFood")
    public String deleteFood(
            @RequestParam(name = "food_id") Long id
    ){
        Food food = foodRepository.getOne(id);
        foodRepository.deleteById(food.getId());
        return "redirect:/";
    }

    @GetMapping(path = "/editFood/{id}")
    public String editFood(ModelMap model, @PathVariable(name = "id") Long id){
        Food food = foodRepository.getOne(id);
        model.addAttribute("food", food);
        return "editFoodPage";
    }

    @PostMapping(value = "/editFood")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR')")
    public String editFood(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "price") int price,
            @RequestParam(name = "food_id") Long food_id

    ){
        Food food = foodRepository.getOne(food_id);
        food.setName(name);
        food.setDescription(description);
        food.setPrice(price);
        foodRepository.save(food);
        return "redirect:/";
    }

    @GetMapping(path = "/addComment/{restaurant_id}")
    public String addComment(ModelMap model, @PathVariable(name = "restaurant_id") Long restaurant_id){
        Restaurants restaurant = restaurantRepository.getOne(restaurant_id);
        model.addAttribute("user", getUserData());
        model.addAttribute("restaurant", restaurant);
        return "addCommentPage";
    }

    @PostMapping(value = "/addComment")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public String addComment(
            @RequestParam(name = "comment") String comment,
            @RequestParam(name = "restaurant_id") Long restaurant_id,
            @RequestParam(name = "user_id") Long user_id

    ){

        Users user = userRepository.getOne(user_id);
        Restaurants restaurant = restaurantRepository.getOne(restaurant_id);
        commentsRepository.save(new Comments(null,user,restaurant,comment,new Date(Calendar.getInstance().getTimeInMillis())));
        return "redirect:/";
    }

    @GetMapping(path = "/editComment/{id}")
    public String editComment(ModelMap model, @PathVariable(name = "id") Long id){
        Comments comment = commentsRepository.getOne(id);
        model.addAttribute("comment", comment);
        return "editCommentPage";
    }

    @PostMapping(value = "/editComment")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public String editComment(
            @RequestParam(name = "comment") String comment,
            @RequestParam(name = "id") Long id

    ){
        Comments comments = commentsRepository.getOne(id);
        comments.setComment(comment);
        comments.setPostDate(new Date(Calendar.getInstance().getTimeInMillis()));
        commentsRepository.save(comments);
        return "redirect:/";
    }

    @PostMapping(value = "/deleteComment")
    public String deleteComment(
            @RequestParam(name = "id") Long id
    ){
        Comments comments = commentsRepository.getOne(id);
        commentsRepository.deleteById(comments.getId());
        return "redirect:/";
    }

    @GetMapping(path = "/editRestaurant/{restaurant_id}")
    public String editRestaurant(ModelMap model, @PathVariable(name = "restaurant_id") Long restaurant_id){
        Restaurants restaurant = restaurantRepository.getOne(restaurant_id);
        model.addAttribute("restaurant", restaurant);
        return "editRestaurantPage";
    }

    @PostMapping(value = "/editRestaurant")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String editRestaurant(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "category") String category,
            @RequestParam(name = "restaurant_id") Long restaurant_id

    ){
        Restaurants restaurant = restaurantRepository.getOne(restaurant_id);
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setCategory(category);
        restaurantRepository.save(restaurant);
        return "redirect:/";
    }

    @PostMapping(value = "/deleteRestaurant")
    public String deleteRestaurant(
            @RequestParam(name = "restaurant_id") Long restaurant_id
    ){
        Restaurants restaurant = restaurantRepository.getOne(restaurant_id);
        List<Food> food = foodRepository.findAll();
        Iterator <Food> iter = food.iterator();
        while(iter.hasNext()){
            Food element = iter.next();
            if (element.getRestaurant().getId() != restaurant.getId()) {
                iter.remove();
            }
        }
        for (Food f : food)
        {
            foodRepository.delete(f);
        }
        List<Comments> comments = commentsRepository.findAll();
        Iterator <Comments> iter2 = comments.iterator();
        while(iter2.hasNext()){
            Comments element = iter2.next();
            if (element.getRestaurant().getId() != restaurant.getId()) {
                iter2.remove();
            }

        }
        for (Comments c : comments)
        {
            commentsRepository.delete(c);
        }
        restaurantRepository.deleteById(restaurant.getId());

        return "redirect:/";
    }

    @GetMapping(path = "/details/{restaurant_id}")
    public String details(ModelMap model, @PathVariable(name = "restaurant_id") Long restaurant_id){
        Restaurants restaurant = restaurantRepository.getOne(restaurant_id);
        List<Comments> comments = commentsRepository.findAll();
        List<Food> food = foodRepository.findAll();
        Iterator <Comments> iter = comments.iterator();
        while(iter.hasNext()){
            Comments element = iter.next();
                if (element.getRestaurant().getId() != restaurant.getId()) {
                    iter.remove();
               }
        }
        Iterator <Food> iter2 = food.iterator();
        while(iter2.hasNext()){
            Food element = iter2.next();
            if (element.getRestaurant().getId() != restaurant.getId()) {
                iter2.remove();
            }
        }
        model.addAttribute("food", food);
        model.addAttribute("user", getUserData());
        model.addAttribute("comment", comments);
        model.addAttribute("restaurant", restaurant);
        return "details";

    }

    @GetMapping(path = "/login")
    public String loginPage(Model model){
        Users user = getUserData();

        return "login";
    }
    @GetMapping(path = "/invalidRegisterPage")
    public String invalidRegisterPage(Model model){
        Users user = getUserData();

        return "invalidRegisterPage";
    }
    @GetMapping(path = "/register")
    public String register(Model model){
        return "register";
    }

    @PostMapping(path = "/addUser")
    public String addUser(@RequestParam(name = "email") String email,
                          @RequestParam(name = "password") String password,
                          @RequestParam(name = "re_password") String rePassword,
                          @RequestParam(name = "name", required = false, defaultValue = "") String name,
                          @RequestParam(name = "surName", required = false, defaultValue = "") String surName
    ){

        //String redirect = "redirect:/register?error";
        String redirect = "redirect:/invalidRegisterPage";
        Users user = userRepository.findByEmail(email);
        if(email.length()<=3 || password.length()<6 || name.length()<1 || surName.length()<1){
            return redirect;
        }
        if(user==null){

            if(password.equals(rePassword)){

                Set<Roles> roles = new HashSet<>();
                Roles userRole = roleRepository.getOne(2L);
                roles.add(userRole);

                user = new Users(null, email, password, name, surName, true, roles);
                userService.registerUser(user);
                redirect = "redirect:/register?success";

            }

        }

        return redirect;

    }

    @GetMapping(path = "/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePage(Model model){

        model.addAttribute("user", getUserData());
        return "profile";

    }

    @GetMapping(path = "/editProfile/{user_id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public String editProfile(ModelMap model, @PathVariable(name = "user_id") Long user_id){
        Users user = getUserData();
        model.addAttribute("user", user);
        return "editProfilePage";
    }

    @PostMapping(value = "/editProfile")
    public String editProfile(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "surname") String surname,
            @RequestParam(name = "email") String email

    ){
        Users user = getUserData();
        user.setName(name);
        user.setSurname(surname);
        if(email.length()<3){
            return "login";
        }
        user.setEmail(email);
        userRepository.save(user);
        return "login";
    }
    @PostMapping(path = "/changeStatus")
    public String changeStatus(@RequestParam(name = "user_id") Long user_id,
                                   @RequestParam(name = "status") int status
    ){
        Users users = userRepository.getOne(user_id);

        if(status == 0){
            users.setActive(false);
        }else if(status == 1){
            users.setActive(true);
        }
        System.out.println(users.isActive());
        userRepository.save(users);
        return "redirect:/userDetails/"+user_id;
    }
    @PostMapping(path = "/changeStatusModerator")
    public String changeStatusModerator(@RequestParam(name = "user_id") Long user_id,
                               @RequestParam(name = "status") int status
    ){
        Users users = userRepository.getOne(user_id);

        if(status == 0){
            users.setActive(false);
        }else if(status == 1){
            users.setActive(true);
        }
        System.out.println(users.isActive());
        userRepository.save(users);
        return "redirect:/moderatorDetails/"+user_id;
    }
    @GetMapping(path = "/addUserFromAdminPage")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addUserFromAdminPage(Model model){
        return "addUserFromAdminPage";
    }

    @PostMapping(path = "/addUserFromAdmin")
    public String addUserFromAdmin(@RequestParam(name = "email") String email,
                          @RequestParam(name = "password") String password,
                          @RequestParam(name = "re_password") String rePassword,
                          @RequestParam(name = "name", required = false, defaultValue = "") String name,
                          @RequestParam(name = "surName", required = false, defaultValue = "") String surName,
                          @RequestParam(name = "role", required = false, defaultValue = "") int role
    ){

        String redirect = "redirect:/profile";
        Users user = userRepository.findByEmail(email);
        if(email.length()<=3 || password.length()<6 || name.length()<1 || surName.length()<1){
            return redirect;
        }
        if(user==null){
            if(password.equals(rePassword)){
                Set<Roles> roles = new HashSet<>();
                if(role == 2) {
                    Roles userRoleUser = roleRepository.getOne(2L);
                    Roles userRoleModerator = roleRepository.getOne(3L);
                    roles.add(userRoleUser);
                    roles.add(userRoleModerator);
                }
                else if(role == 1){
                    Roles userRoleUser = roleRepository.getOne(2L);
                    roles.add(userRoleUser);
                }
                user = new Users(null, email, password, name, surName, true, roles);
                userService.registerUser(user);
                redirect = "redirect:/profile";
            }

        }
        return redirect;
    }

    @GetMapping(path = "/users")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')")
    public String usersPage(Model model){

        model.addAttribute("user", getUserData());

        List<Users> usersList = userRepository.findAll();

        Iterator <Users> iter = usersList.iterator();
        while(iter.hasNext()){
            Users element = iter.next();
            for (Roles roles: element.getRoles()) {
                if (roles == roleRepository.getOne(1L) || roles == roleRepository.getOne(3L)) {
                    iter.remove();
                }
            }
        }
        model.addAttribute("userList", usersList);
        return "usersFromAdmin";
    }

    @GetMapping(path = "/moderators")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String moderatorsPage(Model model){

        model.addAttribute("user", getUserData());

        List<Users> usersList = userRepository.findAll();
        List<Users> moderatorsList = new ArrayList<>();
        Iterator <Users> iter = usersList.iterator();
        while(iter.hasNext()){
            Users element = iter.next();
            for (Roles roles: element.getRoles()) {
                if (roles == roleRepository.getOne(1L)) {
                    iter.remove();
                }
                if (roles == roleRepository.getOne(3L)) {
                    moderatorsList.add(element);
                }
                for (Roles roles1: element.getRoles()) {
                    roles1.getId();
                }
            }
        }
        System.out.println(moderatorsList);
        model.addAttribute("userList", moderatorsList);
        return "moderatorsFromAdmin";
    }

    @GetMapping(path = "/refreshPassword/{user_id}")
    public String refreshPasswordForModerator(ModelMap model, @PathVariable(name = "user_id") Long user_id){
        Users users = userRepository.getOne(user_id);
        model.addAttribute("users", users);
        return "refreshPasswordPage";
    }

    @PostMapping(path = "/refreshPassword")
    public String refreshPassword(@RequestParam(name = "id") Long user_id,
                                   @RequestParam(name = "user_old_password") String user_old_password,
                                   @RequestParam(name = "user_new_password") String user_new_password,
                                   @RequestParam(name = "user_new_repassword") String user_new_repassword

    ){
        Users user = userRepository.getOne(user_id);
        if(user_new_password.length()<6){
            return "redirect:/";
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(user_new_password);
        if(encoder.matches(user_old_password, user.getPassword())){
            if(user_new_password.equals(user_new_repassword)){

                user.setPassword(encodedPassword);
                System.out.println(encodedPassword);
            }
        }
        userRepository.save(user);
        return "redirect:/";
    }

    @GetMapping(path = "/moderatorDetails/{user_id}")
    public String moderatorDetails(ModelMap model, @PathVariable(name = "user_id") Long user_id){
        Users users = userRepository.getOne(user_id);
        model.addAttribute("users", users);
        return "moderatorDetails";
    }

    @GetMapping(path = "/userDetails/{user_id}")
    public String userDetails(ModelMap model, @PathVariable(name = "user_id") Long user_id){
        Users users = userRepository.getOne(user_id);
        model.addAttribute("users", users);
        return "userDetails";
    }


    public Users getUserData(){
        Users userData = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            User secUser = (User)authentication.getPrincipal();
            userData = userRepository.findByEmail(secUser.getUsername());
        }
        return userData;
    }

}