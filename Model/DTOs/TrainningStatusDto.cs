using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Model.DTOs
{
    public class TrainingStatusDto
    {
        public string TrainingStatus { get; set; }
        public int CurrentEpochs { get; set; }
        public List<string> NewLogs { get; set; }
    }
}
