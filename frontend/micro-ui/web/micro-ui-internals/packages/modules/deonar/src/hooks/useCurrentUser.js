import React, { useState, useEffect } from "react";

const useCurrentUser = () => {
    const [currentUser, setCurrentUser] = useState(0);
    //added again

    useEffect(() => {
        const fetchCurrentUser = async () => {
            try {
                //const response = await fetch(collectionRemovalFeeAmt);
                //const totalFee = await response.json(); // Assuming the response is in JSON format
                const curUser = "John Doe";
                console.log(curUser);
                setCurrentUser(curUser);
            } catch (error) {
                console.error("Error fetching current user:", error);
            }
        };

        fetchCurrentUser();
    }, []);

    return currentUser;
};

export default useCurrentUser;