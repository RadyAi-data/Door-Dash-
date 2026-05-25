# DooR DasH: Scare vs Laugh Touchdown 🚪

An interactive, competitive board game built entirely in **Java** and **JavaFX**. Inspired by the Monsters Inc. universe, this project pits "Scarers" against "Laughers" in a strategic race across a dynamic 100-cell grid. 

This project was developed to demonstrate strong Object-Oriented Programming (OOP) concepts, robust architectural design, and state-driven graphical user interfaces.

## 🛠️ Tech Stack & Architecture
* **Language:** Java
* **GUI Framework:** JavaFX
* **Architecture:** Model-View-Controller (MVC) Pattern
* **Data Management:** Dynamic File I/O (CSV Parsing)

## ⚙️ Key Technical Features
* **Strict OOP Design:** Heavy utilization of abstract classes, interfaces, inheritance, and polymorphism to manage diverse game entities (Monsters, Cells, Cards).
* **Custom Exception Handling:** Engineered a custom hierarchy of exceptions (e.g., `InvalidMoveException`, `OutOfEnergyException`, `InvalidCSVFormat`) to ensure stable game loops and secure data loading.
* **Dynamic Board Generation:** The 100-cell zigzag board is generated programmatically at runtime, populating distinct cell types (Doors, Conveyor Belts, Contamination Socks) from external `.csv` datasets.
* **State-Driven UI:** The JavaFX View seamlessly listens to the backend Model, updating player tokens, energy visualizers, and card draw animations in real-time.
* **Game Engine Logic:** Handled complex, overlapping states such as multi-turn status effects (Freeze, Momentum, Focus), shield damage mitigation, and team-wide energy distribution.

## 🎮 Gameplay Overview
Two players navigate a hazardous Floor to reach Boo's Door (Cell 99) with at least 1000 energy. Players must balance the risk of drawing unpredictable action cards and landing on hostile Monster Cells with the reward of utilizing their character's unique active powerups and passive traits.

## 🚀 How to Run Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/RadyAi-data/Door-Dash.git


