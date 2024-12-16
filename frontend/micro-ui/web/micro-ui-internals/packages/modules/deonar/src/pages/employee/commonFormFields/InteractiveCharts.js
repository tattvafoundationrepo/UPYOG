import React, { useState } from 'react';
import { Pie, Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale,
} from 'chart.js';

// Register Chart.js components
ChartJS.register(ArcElement, Tooltip, Legend, BarElement, CategoryScale, LinearScale);



const InteractiveCharts = ({ pieChartData, detailsData }) => {
  // Shared state for the selected segment
  const [selectedData, setSelectedData] = useState(null);

  // Bar Chart data based on selected Pie Chart segment
  const barData = selectedData
    ? {
        labels: detailsData[selectedData.label].labels,
        datasets: [
          {
            label: `Details for ${selectedData.label}`,
            data: detailsData[selectedData.label].data,
            backgroundColor: '#36A2EB',
          },
        ],
      }
    : {
        labels: [],
        datasets: [],
      };

  // Handle pie chart clicks
  const handlePieClick = (event, elements) => {
    if (elements.length > 0) {
      const index = elements[0].index; // Get the clicked segment index
      const label = pieChartData.labels[index];
      setSelectedData({ label });
    }
  };

  return (
    <div>
      <h2>Interactive Charts</h2>
      <div style={{ display: 'flex', justifyContent: 'space-around' }}>
        {/* Pie Chart */}
        <div>
          <h3>Pie Chart</h3>
          <Pie
            data={pieChartData}
            options={{
              onClick: handlePieClick, // Attach the click handler
            }}
          />
        </div>

        {/* Bar Chart */}
        <div>
          <h3>Bar Chart</h3>
          {selectedData ? (
            <Bar data={barData} />
          ) : (
            <p>Click a segment in the Pie Chart to view details.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default InteractiveCharts;