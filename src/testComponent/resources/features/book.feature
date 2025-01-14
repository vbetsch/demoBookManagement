Feature: the user can create and retrieve the books
  Scenario: user creates two books and retrieve both of them
    When the user creates the book "Les Misérables" written by "Victor Hugo"
    And the user creates the book "L'avare" written by "Molière"
    And the user get all books
    Then the list should contains the following books in the same order
      | name | author |
      | L'avare | Molière |
      | Les Misérables | Victor Hugo |