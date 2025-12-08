#include <algorithm>
#include <chrono>
#include <curses.h>
#include <iostream>
#include <random>
#include <stdio.h>
#include <thread>
#include <vector>

#define HEIGHT 20
#define WIDTH 40

class GameObject
{
protected:
    int x, y;
    char symbol;
    bool active;
    int draw_priority;

public:
    GameObject(int startX, int startY, char sym, int draw_priority = 0)
        : x(startX)
        , y(startY)
        , symbol(sym)
        , active(true)
        , draw_priority(draw_priority)
    { }

    virtual ~GameObject() = default;

    virtual void Update(std::vector<std::shared_ptr<GameObject>>& objects) = 0;

    virtual void Draw(WINDOW* win)
    {
        if (active) {
            mvwaddch(win, y, x, symbol);
        }
    }

    virtual void HandleCollision(GameObject* other) = 0;

    int GetX() const
    {
        return x;
    }
    int GetY() const
    {
        return y;
    }
    char GetSymbol() const
    {
        return symbol;
    }
    bool IsActive() const
    {
        return active;
    }

    int getDrawPriority() const
    {
        return draw_priority;
    }

    bool CollidesWith(const GameObject* other) const
    {
        return active && other->IsActive() && x == other->GetX()
               && y == other->GetY();
    }

    bool Move(int dx, int dy, int maxX, int maxY)
    {
        int new_x = x + dx;
        int new_y = y + dy;

        if (new_x >= 1 && new_x < maxX - 1 && new_y >= 1 && new_y < maxY - 1) {
            x = new_x;
            y = new_y;
            return true;
        }
        return false;
    }
};

class Wall : public GameObject
{
public:
    Wall(int x, int y)
        : GameObject(x, y, '#', 1)
    { }
    void Update(std::vector<std::shared_ptr<GameObject>>& objects) override
    { }
    void HandleCollision(GameObject* other) override
    { }
};

class Dot : public GameObject
{

public:
    Dot(int x, int y)
        : GameObject(x, y, '.', 0)
    { }
    void Update(std::vector<std::shared_ptr<GameObject>>& objects) override
    { }

    void HandleCollision(GameObject* other) override
    { }

    void consume()
    {
        active = false;
    }

    void Draw(WINDOW* win) override
    {
        if (active) {
            wattron(win, COLOR_PAIR(3));
            mvwaddch(win, y, x, symbol);
            wattroff(win, COLOR_PAIR(3));
        }
    }
};

class Ghost : public GameObject
{
private:
    int dx, dy;
    std::mt19937 rng;
    std::uniform_int_distribution<int> dir_dist;
    int move_counter;

public:
    Ghost(int x, int y)
        : GameObject(x, y, 'G', 2)
        , dx(0)
        , dy(0)
        , rng(std::random_device {}())
        , dir_dist(0, 3)
        , move_counter(0)
    { }

    void Update(std::vector<std::shared_ptr<GameObject>>& objects) override
    {
        move_counter++;

        if (move_counter % 10 == 0) {
            ChangeDirection();
            move_counter = 0;
        }

        if (dx != 0 || dy != 0) {
            int new_x = x + dx;
            int new_y = y + dy;
            bool can_move = true;

            for (auto& obj : objects) {
                if (dynamic_cast<Wall*>(obj.get()) && obj->GetX() == new_x
                    && obj->GetY() == new_y)
                {
                    can_move = false;
                    break;
                }
            }

            if (can_move) {
                x = new_x;
                y = new_y;
            }
            else {
                ChangeDirection();
            }
        }
    }

    void ChangeDirection()
    {
        int dir = dir_dist(rng);
        switch (dir) {
        case 0:
            dx = -1;
            dy = 0;
            break;
        case 1:
            dx = 1;
            dy = 0;
            break;
        case 2:
            dx = 0;
            dy = -1;
            break;
        case 3:
            dx = 0;
            dy = 1;
            break;
        }
    }

    void HandleCollision(GameObject* other) override
    { }

    void Draw(WINDOW* win) override
    {
        if (active) {
            wattron(win, COLOR_PAIR(2));
            mvwaddch(win, y, x, symbol);
            wattroff(win, COLOR_PAIR(2));
        }
    }
};

class Pacman : public GameObject
{
private:
    int score;
    int lives;
    int dx, dy;
    int next_dx, next_dy;

public:
    Pacman(int x, int y)
        : GameObject(x, y, 'C', 3)
        , score(0)
        , lives(3)
        , dx(0)
        , dy(0)
        , next_dx(0)
        , next_dy(0)
    { }

    void SetDirection(int new_dx, int new_dy)
    {
        next_dx = new_dx;
        next_dy = new_dy;
    }

    void Update(std::vector<std::shared_ptr<GameObject>>& objects) override
    {
        int test_x = x + next_dx;
        int test_y = y + next_dy;
        bool can_change = true;

        for (auto& obj : objects) {
            if (dynamic_cast<Wall*>(obj.get()) && obj->GetX() == test_x
                && obj->GetY() == test_y)
            {
                can_change = false;
                break;
            }
        }

        if (can_change && (next_dx != 0 || next_dy != 0)) {
            dx = next_dx;
            dy = next_dy;
        }

        if (dx != 0 || dy != 0) {
            int new_x = x + dx;
            int new_y = y + dy;
            bool can_move = true;

            for (auto& obj : objects) { 
                if (dynamic_cast<Wall*>(obj.get()) && obj->GetX() == new_x
                    && obj->GetY() == new_y)
                {
                    can_move = false;
                    break;
                }
            }

            if (can_move) {
                x = new_x;
                y = new_y;
            }
            else {
                dx = dy = 0;
                next_dx = next_dy = 0;
            }
        }

        for (auto& obj : objects) {
            if (this != obj.get() && CollidesWith(obj.get())) {
                HandleCollision(obj.get());
                obj->HandleCollision(this);
            }
        }
    }

    void HandleCollision(GameObject* other) override
    {
        if (dynamic_cast<Dot*>(other)) {
            Dot* dot = dynamic_cast<Dot*>(other);
            if (dot) {
                dot->consume();
                score += 10;
            }
        }
        else if (dynamic_cast<Ghost*>(other)) {
            lives--;
            score -= 10;
            if (lives > 0) {

                x = 10;
                y = 10;
                dx = dy = next_dx = next_dy = 0;
            }
            else {
                active = false;
            }
        }
    }

    void Draw(WINDOW* win) override
    {
        if (active) {
            wattron(win, COLOR_PAIR(1));
            mvwaddch(win, y, x, symbol);
            wattroff(win, COLOR_PAIR(1));
        }
    }

    int GetScore() const
    {
        return score;
    }

    int GetLives() const
    {
        return lives;
    }

    bool IsAlive() const
    {
        return lives > 0;
    }
};

class Game
{
private:
    WINDOW* game_win;
    WINDOW* info_win;
    int width, height;
    std::vector<std::shared_ptr<GameObject>> objects;
    bool running;
    Pacman* pacman;

public:
    Game()
        : running(true)
    {
        initscr();
        cbreak();
        noecho();
        curs_set(0);
        timeout(100);
        keypad(stdscr, TRUE);
        height = HEIGHT;
        width = WIDTH;
        if (has_colors()) {
            start_color();
            init_pair(1, COLOR_YELLOW, COLOR_BLACK); // pacman
            init_pair(2, COLOR_RED, COLOR_BLACK); // ghost
            init_pair(3, COLOR_WHITE, COLOR_BLACK); // dot
            init_pair(4, COLOR_CYAN, COLOR_BLACK); // wall
        }

        game_win = newwin(height, width, 0, 0);
        info_win = newwin(3, width, height + 2, 0);

        CreateLevel();
    }

    ~Game()
    {
        delwin(game_win);
        delwin(info_win);
        endwin();
    }

    void CreateLevel()
    {
        // border
        for (int x = 0; x < width; x++) {
            objects.push_back(std::make_shared<Wall>(x, 0));
            objects.push_back(std::make_shared<Wall>(x, height - 1));
        }
        for (int y = 0; y < height; y++) {
            objects.push_back(std::make_shared<Wall>(0, y));
            objects.push_back(std::make_shared<Wall>(width - 1, y));
        }

        // inner walls
        for (int x = 2; x < width - 2; x++) {
            if (x <= 24 || x >= 37) {
                objects.push_back(std::make_shared<Wall>(x, 5));
                objects.push_back(std::make_shared<Wall>(x, 8));
            }

            if (x <= 7 || x >= 13) {

                objects.push_back(std::make_shared<Wall>(x, 15));
                objects.push_back(std::make_shared<Wall>(x, 12));
            }
        }
        for (int y = 2; y < HEIGHT - 2; y++) {
            if (y <= 3 || y >= 7) {
                objects.push_back(std::make_shared<Wall>(5, y));
                objects.push_back(std::make_shared<Wall>(6, y));
            }

            if (y <= 6 || y >= 17) {

                objects.push_back(std::make_shared<Wall>(15, y));
                objects.push_back(std::make_shared<Wall>(12, y));
            }
        }

        auto pac = std::make_shared<Pacman>(10, 10);
        pacman = pac.get();
        objects.push_back(pac);

        objects.push_back(std::make_shared<Ghost>(4, 7));
        objects.push_back(std::make_shared<Ghost>(15, 7));
        objects.push_back(std::make_shared<Ghost>(4, 13));
        objects.push_back(std::make_shared<Ghost>(15, 13));

        for (int x = 2; x < width - 2; x += 1) {
            for (int y = 2; y < height - 2; y += 1) {
                bool occupied = false;
                for (auto& obj : objects) {
                    if ((dynamic_cast<Wall*>(obj.get())
                         || dynamic_cast<Ghost*>(obj.get()))
                        && obj->GetX() == x && obj->GetY() == y)
                    {
                        occupied = true;
                        break;
                    }
                }
                if (!occupied && (x != 10 || y != 10)) {
                    objects.push_back(std::make_shared<Dot>(x, y));
                }
            }
        }
    }

    void ProcessInput()
    {
        int input = getch();
        switch (input) {
        case KEY_UP:
            pacman->SetDirection(0, -1);
            break;
        case KEY_DOWN:
            pacman->SetDirection(0, 1);
            break;
        case KEY_LEFT:
            pacman->SetDirection(-1, 0);
            break;
        case KEY_RIGHT:
            pacman->SetDirection(1, 0);
            break;
        case 'q':
        case 'Q':
            running = false;
            break;
        }
        flushinp();
    }

    void Update()
    {
        std::sort(objects.begin(),
                  objects.end(),
                  [](const std::shared_ptr<GameObject>& a,
                     const std::shared_ptr<GameObject>& b) {
                      return a->getDrawPriority() < b->getDrawPriority();
                  });

        for (auto& obj : objects) {
            if (obj->IsActive()) {
                obj->Update(objects);
            }
        }

        objects.erase(
            std::remove_if(objects.begin(),
                           objects.end(),
                           [](const std::shared_ptr<GameObject>& obj) {
                               return !obj->IsActive()
                                      && dynamic_cast<Dot*>(obj.get());
                           }),
            objects.end());

        if (!pacman->IsAlive()) {
            running = false;
        }

        bool dotsRemaining = false;
        for (auto& obj : objects) {
            if (dynamic_cast<Dot*>(obj.get())) {
                dotsRemaining = true;
                break;
            }
        }
        if (!dotsRemaining) {
            running = false;
        }
    }

    void render()
    {
        wclear(game_win);
        wclear(info_win);

        box(info_win, 0, 0);

        for (auto& obj : objects) {
            obj->Draw(game_win);
        }

        mvwprintw(info_win,
                  1,
                  2,
                  "Score: %d | Lives: %d | Q to quit",
                  pacman->GetScore(),
                  pacman->GetLives());

        if (!running) {
            if (pacman->IsAlive()) {
                mvwprintw(
                    info_win, 1, 2, "                                    ");
                mvwprintw(info_win, 1, width / 2 - 6, "YOU WIN!");
            }
            else {
                mvwprintw(
                    info_win, 1, 2, "                                    ");
                mvwprintw(info_win, 1, width / 2 - 6, "GAME OVER!");
            }
        }

        wrefresh(game_win);
        wrefresh(info_win);
    }

    void StartGame()
    {
        while (running) {
            ProcessInput();
            Update();
            render();
            std::this_thread::sleep_for(std::chrono::milliseconds(100));
        }

        timeout(-1);
        getch();
    }
};

int main()
{

    Game pc;
    pc.StartGame();

    return 0;
}
