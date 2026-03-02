document
	.getElementById("btn-send-notification")
	.addEventListener("click", function () {
		const target = document
			.getElementById("notification-target")
			.value.trim();
		const message = document
			.getElementById("notification-message")
			.value.trim();

		if (!message) {
			alert("Please enter a message to send.");
			return;
		}

		const payload = {
			message: message,
			usernames: target ? [target] : [],
		};

		fetch("/api/admin/notifications", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
				"X-CSRF-TOKEN":
					document.querySelector('input[name="_csrf"]')?.value || "",
			},
			body: JSON.stringify(payload),
		})
			.then((response) => {
				if (response.ok) {
					const inputMsg = document.getElementById(
						"notification-message",
					);
					inputMsg.value = "";
					inputMsg.placeholder = "Sent successfully!";
					setTimeout(
						() => (inputMsg.placeholder = "Hello World!"),
						2000,
					);
				} else {
					alert(
						"Failed to send notification. Check console for details.",
					);
				}
			})
			.catch((err) => {
				console.error("Error sending notification:", err);
				alert("Error sending notification.");
			});
	});
