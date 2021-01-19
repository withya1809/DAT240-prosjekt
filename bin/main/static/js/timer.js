// setInterval(counter function, update timer)
var counter = setInterval(function () {

    // getTime() method returns the number of milliseconds between midnight of January 1, 1970 and now
    var now = new Date().getTime();

    // Find the distance between now an the count down date (startDate) and this current time
    var distance = time * 1000; //hvor mange millisekunder som har g�tt siden du starta og n�

    // Calculation for minutes and seconds
    if (!stopTimer) {
        time++;
    }
    var minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60)); // checks how many minutes in the distance
    var seconds = Math.floor((distance % (1000 * 60)) / 1000); // checks how many seconds in the distance
    let output = minutes + "m " + seconds + "s ";
    if (minutes === 0) {
        output = seconds + "s";
    }
    document.getElementById("timer").innerHTML = output;
}, 1000); //runs the fuction every 1000ms = 1 s


// function to stop the timer when the game is done
function StopTimer() {
    clearInterval(counter);
}