<script>
    export let rating = 0;
</script>

<style>
    .star-container {
        display: flex;
        justify-content: center;
        align-items: center;
    }

    .star {
        margin-right: 0.25em;
        width: 2em;
    }

    .starPath {
        fill: none;
        stroke: #999;
    }

    .filled {
        fill: yellow;
    }
    svg { 
        margin-bottom: 0;
    }
</style>

<div class="star-container">
    {#each Array(5).fill(0) as _, i}
        <svg xmlns="http://www.w3.org/2000/svg" class="star" viewBox="0 0 51 48">
            <title>Star</title>
            <path class:filled={i < rating} class="starPath" d="m25,1 6,17h18l-14,11 5,17-15-10-15,10 5-17-14-11h18z" />
        </svg>    
    {/each}
</div>