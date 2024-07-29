#!/bin/bash

# Base URL for the API
BASE_URL="http://localhost:8889/api"

# Function to print a separator
print_separator() {
    echo "--------------------------------------------------"
}

# Array of test cases
declare -a TEST_CASES=(
    "Register a new user with a valid request body"
    "Register second user with a valid request body"
    "Solve an existing quiz with a correct answer for user 1"
    "Solve an existing quiz with a correct answer for user 2"
    "Fetch completed quizzes for user 1"
    "Fetch completed quizzes for user 2"
    "Create a new quiz"
    "Create a new quiz with an empty options array"
    "Solve an existing quiz with a correct answer"
    "Solve an existing quiz with a wrong answer"
    "Solve a non-existing quiz"
    "Request a list of quizzes without providing authentication"
    "Delete a quiz created by the same user"
    "Delete a non-existing quiz"
    "Delete a quiz created by another user"
    "Fetch completed quizzes"
)

# Function to print the list of test cases
print_test_cases() {
    echo "Available test cases:"
    for i in "${!TEST_CASES[@]}"; do
        echo "$((i + 1)). ${TEST_CASES[$i]}"
    done
}

# Function to execute the selected test case
run_test_case() {
    case $1 in
        1)
            print_separator
            echo "Register a new user with a valid request body"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X POST "$BASE_URL/register" -H "Content-Type: application/json" -d '{
              "email": "test@mail.org",
              "password": "strongpassword"
            }'
            ;;
        2)
            print_separator
            echo "Register second user with a valid request body"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X POST "$BASE_URL/register" -H "Content-Type: application/json" -d '{
              "email": "a@a.a",
              "password": "strongpassword"
            }'
            ;;
        3)
            print_separator
            echo "Solve an existing quiz with a correct answer for user 1"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X POST "$BASE_URL/quizzes/1/solve" -H "Content-Type: application/json" -u "test@mail.org:strongpassword" -d '{
              "answer": [2]
            }'
            ;;
        4)
            print_separator
            echo "Solve an existing quiz with a correct answer for user 2"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X POST "$BASE_URL/quizzes/1/solve" -H "Content-Type: application/json" -u "a@a.a:strongpassword" -d '{
              "answer": [2]
            }'
            ;;
        5)
            print_separator
            echo "Fetch completed quizzes for user 1"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X GET "$BASE_URL/quizzes/completed" -u "test@mail.org:strongpassword"
            ;;
        6)
            print_separator
            echo "Fetch completed quizzes for user 2"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X GET "$BASE_URL/quizzes/completed" -u "a@a.a:strongpassword" | jq
            ;;
        7)
            print_separator
            echo "Create a new quiz"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X POST "$BASE_URL/quizzes" -H "Content-Type: application/json" -u "test@mail.org:strongpassword" -d '{
              "title": "QQQ",
              "text": "What is depicted on the Java logo?",
              "options": ["Robot", "Tea leaf", "Cup of coffee", "Bug"],
              "answer": [2]
            }'
            ;;
        8)
            print_separator
            echo "Create a new quiz with an empty options array"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X POST "$BASE_URL/quizzes" -H "Content-Type: application/json" -u "test@mail.org:strongpassword" -d '{
              "title": "The Java Logo",
              "text": "What is depicted on the Java logo?",
              "options": [],
              "answer": [2]
            }'
            ;;
        9)
            print_separator
            echo "Solve an existing quiz with a correct answer"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X POST "$BASE_URL/quizzes/1/solve" -H "Content-Type: application/json" -u "test@mail.org:strongpassword" -d '{
              "answer": [2]
            }'
            ;;
        10)
            print_separator
            echo "Solve an existing quiz with a wrong answer"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X POST "$BASE_URL/quizzes/1/solve" -H "Content-Type: application/json" -u "test@mail.org:strongpassword" -d '{
              "answer": [0]
            }'
            ;;
        11)
            print_separator
            echo "Solve a non-existing quiz"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X POST "$BASE_URL/quizzes/15/solve" -H "Content-Type: application/json" -u "test@mail.org:strongpassword" -d '{
              "answer": [2]
            }'
            ;;
        12)
            print_separator
            echo "Request a list of quizzes without providing authentication"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X GET "$BASE_URL/quizzes"
            ;;
        13)
            print_separator
            echo "Delete a quiz created by the same user"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X DELETE "$BASE_URL/quizzes/2" -u "test@mail.org:strongpassword"
            ;;
        14)
            print_separator
            echo "Delete a non-existing quiz"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X DELETE "$BASE_URL/quizzes/20" -u "test@mail.org:strongpassword"
            ;;
        15)
            print_separator
            echo "Delete a quiz created by another user"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X DELETE "$BASE_URL/quizzes/5" -u "test@mail.org:strongpassword"
            ;;
        16) # Fetch completed quizzes
            print_separator
            echo "Fetch completed quizzes"
            curl -s -w "\nHTTP Code: %{http_code}\n" -X GET "$BASE_URL/quizzes/completed" -u "test@mail.org:strongpassword"
            ;;
        *)
            echo "Invalid test case number"
            ;;
    esac
}

# Main script logic
while true; do
    print_separator
    print_test_cases
    read -p "Enter the number of the test case to run (or 'q' to quit): " test_case_number

    if [[ "$test_case_number" == "q" ]]; then
        echo "Exiting..."
        break
    elif [[ "$test_case_number" =~ ^[0-9]+$ ]]; then
        run_test_case "$test_case_number"
    else
        echo "Invalid input. Please enter a valid test case number."
    fi
done
