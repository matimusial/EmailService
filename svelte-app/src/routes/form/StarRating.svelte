<script>
	export let ratingId;
	export let rating;
	import Star from './Star.svelte';
  
	let hoverRating = null;
	let collectFeedback = false;
	let feedbackCompleted = false;
  
	$: collectFeedback && addWatchListeners();
	$: !collectFeedback && feedbackFormClosed();
  
	function feedbackFormClosed() {
	  feedbackCompleted = false;
	  removeWatchListeners();
	}
  
	function addWatchListeners() {
	  document.addEventListener('keydown', userDismissFeedback);
	  document.addEventListener('click', userClickedOutsideOfFeedback);
	}
  
	function removeWatchListeners() {
	  document.removeEventListener('keydown', userDismissFeedback);
	  document.removeEventListener('click', userClickedOutsideOfFeedback);
	}
  
	function userClickedOutsideOfFeedback(event) {
	  const container = document.getElementById(`feedbackContainer_${ratingId}`);
	  if (!container.contains(event.target)) {
		collectFeedback = false;
	  }
	}
  
	function userDismissFeedback(event) {
	  if (event.key === 'Escape') {
		collectFeedback = false;
	  }
	}
  
	const handleHover = (id) => () => {
	  hoverRating = id;
	};
  
	const handleRate = (id) => (event) => {
	  if (collectFeedback && rating === id) {
		collectFeedback = false;
		return;
	  }
	  rating = id;
	  collectFeedback = true;
	};
  
	let stars = [
	  { id: 1, title: 'One Star' },
	  { id: 2, title: 'Two Stars' },
	  { id: 3, title: 'Three Stars' },
	  { id: 4, title: 'Four Stars' },
	  { id: 5, title: 'Five Stars' },
	];
</script>
  
<style>
	.feedback {
	  position: relative;
	}
	.starContainer {
	  display: inline-block;
	  border-radius: 8px;
	  padding: 4px 6px 0;
	}
	.feedbackContainerDisabled {
	  pointer-events: none;
	}
	:global(button) {
	  cursor: pointer;
	}
</style>
  
<div class="feedback">
	<span id={`feedbackContainer_${ratingId}`} class="feedbackContainer" class:feedbackContainerDisabled={feedbackCompleted}>
		<span class="starContainer">
			{#each stars as star (star.id)}
				<Star 
					filled={hoverRating ? (hoverRating >= star.id) : (rating >= star.id)} 
					starId={star.id}
					on:mouseover={handleHover(star.id)} 
					on:mouseleave={() => hoverRating = null}
					on:click={handleRate(star.id)}
				/>
			{/each}
		</span>
	</span>
</div>
