#!/usr/bin/env bash
set -e

start_api() {
  DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  SUB_DIR="app/build/libs"
  JAR_NAME="app-all.jar"
    echo 'Starting API'
    java -jar "${DIR}"/"${SUB_DIR}"/"${JAR_NAME}" > /dev/null &
    export API_PID="${!}"
    echo "API starting at PID ${API_PID}"
}

wait_for_api() {
    attempt_counter=0
    max_attempts=5

    until curl --output /dev/null --silent --head --fail http://localhost:8080/health; do
        if [ "${attempt_counter}" -eq "${max_attempts}" ];then
            echo 'Done waiting for API to respond'
            exit 1
        fi
        attempt_counter=$(($attempt_counter+1))
        echo 'Waiting for API to respond'
        sleep 5
    done

    echo 'API is responding'
}

run_tests() {
    echo 'Running the end-to-end tests'
    ./gradlew --no-daemon e2e:clean e2e:test
}

cleanup() {
    echo "Killing API at PID ${API_PID}"
    kill "${API_PID}"
}

trap cleanup EXIT  # Run the cleanup function on exit
start_api
wait_for_api
run_tests
